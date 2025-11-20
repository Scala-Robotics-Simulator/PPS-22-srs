import gymnasium.spaces as spaces
import numpy as np
import rl_pb2

from environment.abstract_env import AbstractEnv


class ExplorationEnv(AbstractEnv):
    """Custom environment for Deep Q-Learning Exploration via gRPC"""

    def __init__(
            self,
            server_address,
            client_name,
            grid_size: tuple = (5, 5),
            orientation_bins: int = 8,
            # cell_size: float = 1.0,
    ) -> None:
        super().__init__(server_address, client_name)

        self.actions = [
            # (1.0, 1.0),  # move forward
            # (1.0, -1.0),  # rotate in place clockwise
            # (-1.0, 1.0),  # rotate in place counterclockwise
            # (1.0, 0.5),  # gentle right curve (right wheel slower)
            # (0.5, 1.0),  # gentle left curve (left wheel slower)
            (0.6, 0.6),   # slow forward  — controllo fine in spazi stretti
            (1.0, 1.0),   # fast forward  — per attraversare aree libere
            (0.6, 0.3),   # right curve (moderata) — sterzo a destra
            (0.3, 0.6),   # left curve  (moderata) — sterzo a sinistra
            (0.5, -0.5),  # rotate clockwise slow — rotazione fine
            (-0.5, 0.5),  # rotate ccw slow — rotazione fine
        ]
        self.action_space = spaces.Discrete(len(self.actions))

        self.grid_size = grid_size
        self.orientation_bins = orientation_bins

        # osservazione: x_norm, y_norm, orientation_norm, is_new + 8 proximity = 12
        self.observation_space = spaces.Box(
            low=0.0, high=1.0, shape=(4,), dtype=np.float32
        )
        # self.cell_size = cell_size
        # self.visited = set()

    def _discrete_cell(self, position):
        cell_x = int(position.x / self.cell_size)
        cell_y = int(position.y / self.cell_size)
        return cell_x, cell_y

    def _encode_observation(self, proximity_values, light_values, position, orientation):
        # normalizzazione posizione/orientazione
        # x_norm = np.clip(position.x / (self.grid_size[0] - 1), 0.0, 1.0)
        # y_norm = np.clip(position.y / (self.grid_size[1] - 1), 0.0, 1.0)
        # orientation_norm = (orientation % 360.0) / 360.0
        x_norm = np.clip(position.x / self.grid_size[0], 0.0, 1.0)
        y_norm = np.clip(position.y / self.grid_size[1], 0.0, 1.0)
        orientation_sin = np.sin(np.radians(orientation))
        orientation_cos = np.cos(np.radians(orientation))

        # cell = self._discrete_cell(position)
        # is_new = 1.0 if cell not in self.visited else 0.0
        # self.visited.add(cell)

        obs = np.concatenate([
            np.array([x_norm, y_norm, orientation_sin, orientation_cos], dtype=np.float32),
        ])

        assert obs.shape[0] == 4, f"Observation must have length 12 but got {obs.shape[0]}"
        return obs

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
