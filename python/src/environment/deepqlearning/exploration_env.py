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
    ) -> None:
        super().__init__(server_address, client_name)

        self.actions = [
            (1.0, 1.0),  # move forward
            (1.0, -1.0),  # rotate in place clockwise
            (-1.0, 1.0),  # rotate in place counterclockwise
            (1.0, 0.0),  # gentle right curve (right wheel slower)
            (0.0, 1.0),  # gentle left curve (left wheel slower)
        ]
        self.action_space = spaces.Discrete(len(self.actions))

        self.grid_size = grid_size
        self.orientation_bins = orientation_bins

        self.observation_space = spaces.Box(
            low=0.0, high=1.0, shape=(37,), dtype=np.float32
        )

    def _discrete_cell(self, position):
        cell_x = int(position.x / self.cell_size)
        cell_y = int(position.y / self.cell_size)
        return cell_x, cell_y

    def _encode_observation(self, proximity_values, light_values, position, orientation, visited_positions):
        # TODO
        # x_norm = np.clip(position.x / (self.grid_size[0] - 1), 0.0, 1.0)
        # y_norm = np.clip(position.y / (self.grid_size[1] - 1), 0.0, 1.0)
        # orientation_norm = (orientation % 360.0) / 360.0
        x_norm = np.clip(position.x / self.grid_size[0], 0.0, 1.0)
        y_norm = np.clip(position.y / self.grid_size[1], 0.0, 1.0)
        orientation_sin = np.sin(np.radians(orientation))
        orientation_cos = np.cos(np.radians(orientation))
        obs = np.concatenate([
            np.array([x_norm, y_norm, orientation_sin, orientation_cos, *visited_positions, *proximity_values], dtype=np.float32),
        ])

        assert obs.shape[0] == 37, f"Observation must have length 37 but got {obs.shape[0]}"
        return obs

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
