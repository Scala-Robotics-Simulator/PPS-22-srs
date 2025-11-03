import gymnasium.spaces as spaces
import rl_pb2

from environment.abstract_env import AbstractEnv
from utils.log import Logger
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from collections.abc import Callable

logger = Logger(__name__)


class PhototaxisEnv(AbstractEnv):
    _CARDINAL_IDX = [0, 2, 4, 6]  # Front, Right, Back, Left
    _NO_LIGHT_THRESHOLD = 0.01  # REQUIRED SYNC WITH REWARD FUNCTION

    def __init__(
        self,
        server_address: str,
        client_name: str,
        encoder_name: str = "light9",
        action_set: str = "gentle4",
    ) -> None:
        super().__init__(server_address, client_name)

        self._encoders = self._build_encoder_registry()
        self._actions = self._build_action_registry()

        # encoder
        if encoder_name not in self._encoders:
            logger.warn(
                f"[ModularRobotEnv] unknown encoder '{encoder_name}', using 'light4'"
            )
            encoder_name = "light4"
        self.encoder_name = encoder_name
        enc_meta = self._encoders[encoder_name]
        self._encode_fn: Callable[[list[float] | None, list[float]], int] = enc_meta[
            "fn"
        ]
        n_states: int = enc_meta["n"]

        # actions
        if action_set not in self._actions:
            logger.warn(
                f"[ModularRobotEnv] unknown action_set '{action_set}', using 'gentle4'"
            )
            action_set = "gentle4"
        self.action_set = action_set
        self.actions = self._actions[action_set]["actions"]

        # spaces
        self.observation_space = spaces.Discrete(n_states)
        self.observation_space_n = n_states
        self.action_space = spaces.Discrete(len(self.actions))

        logger.info(
            f"[ModularRobotEnv] encoder='{self.encoder_name}' ({n_states} states), "
            f"action_set='{self.action_set}'"
        )

    def _build_encoder_registry(self) -> dict[str, dict]:
        return {
            # 4 states: F,R,B,L
            "light4": {
                "fn": self._enc_light4,
                "n": 4,
            },
            # 8 states: argmax on eight lights
            "light8": {
                "fn": self._enc_light8,
                "n": 8,
            },
            # 9 states: 8 for a light direction, 1 for "no light"
            "light9": {
                "fn": self._enc_light9,
                "n": 9,
            },
            # 4 * 3 * 3 = 36
            "compact36": {
                "fn": self._enc_phototaxis_compact36,
                "n": 36,
            },
            # canonic OA : 3^8
            "prox8_3bins": {
                "fn": self._enc_prox8_3bins,
                "n": 3**8,
            },
            # light plus two-obstacle warnings: 16
            "light4_plus_flags": {
                "fn": self._enc_light4_plus_flags,
                "n": 16,
            },
        }

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

    # OBSERVATION
    def _encode_observation(
        self,
        proximity_values: list[float] | None,
        light_values: list[float],
    ):
        # come OA: fai solo encode e torna un int
        # (qui aggiungiamo solo un po' di robustness per sensori < 8)
        if len(light_values) < 8:
            light_values = light_values + [0.0] * (8 - len(light_values))
        if proximity_values is not None and len(proximity_values) < 8:
            proximity_values = proximity_values + [1.0] * (8 - len(proximity_values))

        return self._encode_fn(proximity_values, light_values)

    # ACTION
    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=float(left), right_wheel=float(right))

    # ENCODERS IMPLs
    def _enc_light4(self, _prox: list[float] | None, light: list[float]) -> int:
        forward, right, back, left = [light[i] for i in self._CARDINAL_IDX]
        vals = [forward, right, back, left]
        return int(max(range(4), key=lambda k: vals[k]))

    @staticmethod
    def _enc_light8(_prox: list[float] | None, light: list[float]) -> int:
        return int(max(range(8), key=lambda k: light[k]))

    def _enc_phototaxis_compact36(
        self, prox: list[float] | None, light: list[float]
    ) -> int:
        prox = prox or [1.0] * 8
        # front = min(0,1,7); side = min(2,6)
        front_val = min(prox[0], prox[1], prox[7])
        side_val = min(prox[2], prox[6])

        def bin3(x: float) -> int:
            if x < 0.2:
                return 0
            if x < 0.6:
                return 1
            return 2

        b_front = bin3(front_val)
        b_side = bin3(side_val)

        # 4 lights
        forward, right, back, left = [light[i] for i in self._CARDINAL_IDX]
        light_dir = int(max(range(4), key=lambda k: [forward, right, back, left][k]))

        return light_dir * 9 + b_front * 3 + b_side

    @staticmethod
    def _enc_prox8_3bins(prox: list[float] | None, _light: list[float]) -> int:
        prox = prox or [1.0] * 8
        state = 0
        for i, val in enumerate(prox):
            if val < 0.02:
                b = 0
            elif val < 0.2:
                b = 1
            else:
                b = 2
            state += b * (3**i)
        return state

    @staticmethod
    def _enc_light9(_prox: list[float] | None, light: list[float]) -> int:
        max_light_val = max(light)

        if max_light_val < PhototaxisEnv._NO_LIGHT_THRESHOLD:
            return 8
        return int(max(range(8), key=lambda k: light[k]))

    def _enc_light4_plus_flags(
        self, prox: list[float] | None, light: list[float]
    ) -> int:
        prox = prox or [1.0] * 8
        forward, right, back, left = [light[i] for i in self._CARDINAL_IDX]
        vals = [forward, right, back, left]
        light_dir = int(max(range(4), key=lambda k: vals[k]))
        front_clear = int(min(prox[0], prox[1], prox[7]) >= 0.2)
        side_clear = int(min(prox[2], prox[6]) >= 0.2)
        # 4 * 2 * 2 = 16
        return (light_dir * 2 + front_clear) * 2 + side_clear
