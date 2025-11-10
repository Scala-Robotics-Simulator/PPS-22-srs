import gymnasium.spaces as spaces
import numpy as np
import rl_pb2

from environment.abstract_env import AbstractEnv


class ExplorationEnv(AbstractEnv):
    """Custom environment class for RL interaction via gRPC"""

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
            (1.0, 0.5),  # gentle right curve (right wheel slower)
            (0.5, 1.0),  # gentle left curve (left wheel slower)
        ]
        self.action_space = spaces.Discrete(len(self.actions))

        self.grid_size = grid_size
        self.orientation_bins = orientation_bins
        self.observation_space_n = (
            self.grid_size[0] * self.grid_size[1] * self.orientation_bins
        )
        self.observation_space = spaces.Discrete(self.observation_space_n)

    def _encode_observation(
        self, proximity_values, light_values, position, orientation
    ) -> int:
        orientation_step = 360.0 / self.orientation_bins
        orientation_idx = int((orientation % 360) / orientation_step)

        x = int(np.clip(position.x, 0, self.grid_size[0] - 1))
        y = int(np.clip(position.y, 0, self.grid_size[1] - 1))

        state = x
        state += y * self.grid_size[0]
        state += orientation_idx * self.grid_size[0] * self.grid_size[1]
        return state

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
