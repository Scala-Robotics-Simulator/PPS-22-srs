import gymnasium.spaces as spaces
import rl_pb2
from environment.abstract_env import AbstractEnv
from collections.abc import Callable  # typing only
from utils.log import Logger

logger = Logger(__name__)


class PhototaxisEnv(AbstractEnv):
    """
    Fully configurable phototaxis / obstacle-avoidance env.

    State is built as a PRODUCT of a light-part and a prox-part:

        light_part = light_dir_states * light_intensity_bins
        prox_part  = prox_dir_states  * prox_intensity_bins

        total_states = light_part * prox_part

    You can choose:
    - how to encode light direction (4, 8, +no-light)
    - how many bins for light intensity
    - how to encode proximity direction (none, front_min, prox4, prox8, prox4_threat, prox8_threat)
    - how many bins for proximity intensity
    - thresholds for both
    """

    # indices for cardinal sensors (assuming 8 sensors around robot)
    _CARDINAL_IDX = [0, 2, 4, 6]

    def __init__(
        self,
        server_address: str,
        client_name: str = "phototaxis_env",
        *,
        # action
        action_set: str = "gentle4",
        # light
        light_direction: str = "light8",
        light_has_no_light_state: bool = False,
        light_intensity_bins: int = 1,
        # prox
        prox_direction: str = "front_min",
        prox_intensity_bins: int = 3,
        # thresholds
        no_light_threshold: float = 0.01,  # sync with scala reward
        prox_thresholds: list[float] | None = None,
        light_intensity_thresholds: list[float] | None = None,
        # sensor shapes
        num_light_sensors: int = 8,
        num_prox_sensors: int = 8,
    ) -> None:
        super().__init__(server_address, client_name)

        # defaults for thresholds
        if prox_thresholds is None:
            prox_thresholds = [0.03, 0.15]
        if light_intensity_thresholds is None:
            light_intensity_thresholds = [0.2, 0.6]

        # store config
        self.action_set = action_set

        self.light_direction = light_direction
        self.light_has_no_light_state = light_has_no_light_state
        self.light_intensity_bins = light_intensity_bins

        self.prox_direction = prox_direction
        self.prox_intensity_bins = prox_intensity_bins

        self.no_light_threshold = no_light_threshold
        self.prox_thresholds = sorted(prox_thresholds)
        self.light_intensity_thresholds = sorted(light_intensity_thresholds)

        self.num_light_sensors = num_light_sensors
        self.num_prox_sensors = num_prox_sensors

        # ------------------ VALIDATION ------------------
        if (
            self.light_intensity_bins > 1
            and len(self.light_intensity_thresholds) != self.light_intensity_bins - 1
        ):
            raise ValueError(
                f"light_intensity_bins={self.light_intensity_bins} requires "
                f"{self.light_intensity_bins - 1} thresholds, got {len(self.light_intensity_thresholds)}"
            )

        if (
            self.prox_intensity_bins > 1
            and len(self.prox_thresholds) != self.prox_intensity_bins - 1
        ):
            raise ValueError(
                f"prox_intensity_bins={self.prox_intensity_bins} requires "
                f"{self.prox_intensity_bins - 1} thresholds, got {len(self.prox_thresholds)}"
            )

        # ------------------ ACTION SPACE ------------------
        self._actions = self._build_action_registry()
        if self.action_set not in self._actions:
            logger.warning(
                f"[PhototaxisEnv] unknown action_set '{self.action_set}', using 'gentle4'"
            )
            self.action_set = "gentle4"
        self.actions = self._actions[self.action_set]["actions"]
        self.action_space = spaces.Discrete(len(self.actions))

        # ------------------ LIGHT DIR ENCODER ------------------
        self._light_dir_encoder, self.light_dir_states = self._build_light_dir_encoder()

        # ------------------ PROX DIR ENCODER -------------------
        self._prox_dir_encoder, self.prox_dir_states = self._build_prox_dir_encoder()

        # ------------------ STATE SPACE SIZE -------------------
        # light part = light_dir_states * light_intensity_bins
        # prox part  = prox_dir_states  * prox_intensity_bins
        n_states = (self.light_dir_states * self.light_intensity_bins) * (
            self.prox_dir_states * self.prox_intensity_bins
        )

        self._encode_fn = self._master_encoder
        self.observation_space = spaces.Discrete(n_states)
        self.observation_space_n = n_states

        logger.info(
            f"[PhototaxisEnv] Light: {self.light_direction}"
            f" (+no_light={self.light_has_no_light_state}) -> {self.light_dir_states} states"
        )
        logger.info(
            f"[PhototaxisEnv] Prox: {self.prox_direction} -> {self.prox_dir_states} states"
        )
        logger.info(
            f"[PhototaxisEnv] Bins: light={self.light_intensity_bins}, prox={self.prox_intensity_bins}"
        )
        logger.info(f"[PhototaxisEnv] Total discrete states: {n_states}")

    # =====================================================================
    # ACTIONS
    # =====================================================================
    @staticmethod
    def _build_action_registry() -> dict[str, dict]:
        return {
            "gentle4": {
                "actions": [
                    (1.0, 1.0),  # forward
                    (0.6, -0.6),  # strong-left
                    (-0.6, 0.6),  # strong-right
                    (0.0, 0.0),  # stop
                ]
            },
            "hard3": {
                "actions": [
                    (1.0, 1.0),  # forward
                    (1.0, -1.0),  # left
                    (-1.0, 1.0),  # right
                ]
            },
            "pivot6": {
                "actions": [
                    (1.0, 1.0),  # forward
                    (0.8, -0.2),  # gentle left
                    (-0.2, 0.8),  # gentle right
                    (0.6, -0.6),  # strong left
                    (-0.6, 0.6),  # strong right
                    (0.0, 0.0),  # stop
                ]
            },
        }

    def _decode_action(self, action: int):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=float(left), right_wheel=float(right))

    # =====================================================================
    # SENSOR PADDING
    # =====================================================================
    def _pad_light(self, light: list[float]) -> list[float]:
        if len(light) < self.num_light_sensors:
            light = light + [0.0] * (self.num_light_sensors - len(light))
        return light

    def _pad_prox(self, prox: list[float] | None) -> list[float]:
        prox = prox or [1.0] * self.num_prox_sensors
        if len(prox) < self.num_prox_sensors:
            prox = prox + [1.0] * (self.num_prox_sensors - len(prox))
        return prox

    # =====================================================================
    # OBSERVATION ENCODING
    # =====================================================================
    def _encode_observation(
        self,
        proximity_values: list[float] | None,
        light_values: list[float],
    ) -> int:
        prox_padded = self._pad_prox(proximity_values)
        light_padded = self._pad_light(light_values)
        return self._encode_fn(prox_padded, light_padded)

    # =====================================================================
    # MASTER ENCODER
    # =====================================================================
    def _master_encoder(self, prox: list[float], light: list[float]) -> int:
        # --- light part ---
        dir_state = self._light_dir_encoder(light)
        int_state = self._enc_light_intensity(light)
        light_part = (
            dir_state * self.light_intensity_bins + int_state
        )  # 0 .. (L*Li - 1)

        # --- prox part ---
        prox_dir_state = self._prox_dir_encoder(prox)
        prox_int_state = self._enc_prox_intensity(prox)
        prox_part = (
            prox_dir_state * self.prox_intensity_bins + prox_int_state
        )  # 0 .. (P*Pi - 1)

        # combine
        total_prox_states = self.prox_dir_states * self.prox_intensity_bins
        return light_part * total_prox_states + prox_part

    # =====================================================================
    # LIGHT DIR ENCODER BUILDER
    # =====================================================================
    def _build_light_dir_encoder(self) -> (Callable[[list[float]], int], int):
        """Return (encoder_fn, n_states) for light direction."""
        if self.light_direction == "light4":
            base_states = 4
            base_encoder = self._enc_light4
        elif self.light_direction == "light8":
            base_states = 8
            base_encoder = self._enc_light8
        elif self.light_direction == "none":
            # always 0, single state
            return (lambda light: 0), 1
        else:
            raise ValueError(f"Unknown light_direction: {self.light_direction}")

        if self.light_has_no_light_state:
            no_light_state_index = base_states

            def _enc_with_no_light(light: list[float]) -> int:
                if max(light) < self.no_light_threshold:
                    return no_light_state_index
                return base_encoder(light)

            return _enc_with_no_light, base_states + 1
        return base_encoder, base_states

    # =====================================================================
    # PROX DIR ENCODER BUILDER
    # =====================================================================
    def _build_prox_dir_encoder(self) -> (Callable[[list[float]], int], int):
        """
        Return (encoder_fn, n_states) for proximity direction.
        """
        pd = self.prox_direction

        if pd == "none":
            return (lambda prox: 0), 1

        if pd == "front_min":
            # no direction, intensity only
            return (lambda prox: 0), 1

        if pd == "prox4":
            # clearest among F,R,B,L
            return self._enc_prox4_clear, 4

        if pd == "prox8":
            # clearest among 8
            return self._enc_prox8_clear, 8

        if pd == "prox4_threat":
            # nearest threat among F,R,B,L (argmin)
            return self._enc_prox4_threat, 4

        if pd == "prox8_threat":
            # nearest threat among 8 (argmin)
            return self._enc_prox8_threat, 8

        raise ValueError(f"Unknown prox_direction: {pd}")

    # =====================================================================
    # SMALL HELPERS
    # =====================================================================
    @staticmethod
    def _bin_value(value: float, thresholds: list[float]) -> int:
        """
        Generic binning helper.
        thresholds must be sorted.
        returns 0..len(thresholds)
        """
        for i, thr in enumerate(thresholds):
            if value < thr:
                return i
        return len(thresholds)

    # =====================================================================
    # LIGHT DIRECTION ENCODERS
    # =====================================================================
    def _enc_light4(self, light: list[float]) -> int:
        forward, right, back, left = [light[i] for i in self._CARDINAL_IDX]
        vals = [forward, right, back, left]
        return int(max(range(4), key=lambda k: vals[k]))

    def _enc_light8(self, light: list[float]) -> int:
        return int(max(range(8), key=lambda k: light[k]))

    # =====================================================================
    # LIGHT INTENSITY
    # =====================================================================
    def _enc_light_intensity(self, light: list[float]) -> int:
        if self.light_intensity_bins == 1:
            return 0
        max_val = max(light)
        return self._bin_value(max_val, self.light_intensity_thresholds)

    # =====================================================================
    # PROX DIRECTION ENCODERS
    # =====================================================================
    def _enc_prox4_clear(self, prox: list[float]) -> int:
        """Pick direction with max clearance among F,R,B,L."""
        forward, right, back, left = [prox[i] for i in self._CARDINAL_IDX]
        vals = [forward, right, back, left]
        return int(max(range(4), key=lambda k: vals[k]))

    def _enc_prox8_clear(self, prox: list[float]) -> int:
        """Pick direction with max clearance among 8."""
        return int(max(range(8), key=lambda k: prox[k]))

    def _enc_prox4_threat(self, prox: list[float]) -> int:
        """Pick direction with MIN clearance among F,R,B,L (nearest obstacle)."""
        forward, right, back, left= [prox[i] for i in self._CARDINAL_IDX]
        vals = [forward, right, back, left]
        return int(min(range(4), key=lambda k: vals[k]))

    def _enc_prox8_threat(self, prox: list[float]) -> int:
        """Pick direction with MIN clearance among 8 (nearest obstacle)."""
        return int(min(range(8), key=lambda k: prox[k]))

    # =====================================================================
    # PROX INTENSITY
    # =====================================================================
    def _enc_prox_intensity(self, prox: list[float]) -> int:
        """
        Bins how *bad* the current situation is.
        - if prox_direction == 'front_min': bin front triple (0,1,7)
        - else: bin global min (worst sensor)
        """
        if self.prox_intensity_bins == 1:
            return 0

        if self.prox_direction == "front_min":
            val_to_bin = min(prox[0], prox[1], prox[7])
        else:
            # for directional prox we just take the worst of all
            val_to_bin = min(prox)

        return self._bin_value(val_to_bin, self.prox_thresholds)
