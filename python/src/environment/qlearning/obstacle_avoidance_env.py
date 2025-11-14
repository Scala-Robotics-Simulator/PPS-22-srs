import gymnasium.spaces as spaces

import rl_pb2
from environment.abstract_env import AbstractEnv
from utils.log import Logger

logger = Logger(__name__)


class ObstacleAvoidanceEnv(AbstractEnv):
    """Custom environment class for RL interaction via gRPC"""

    def __init__(self, server_address, client_name) -> None:
        super().__init__(server_address, client_name)
        self._bin_values = 4
        self._bin_num = 3
        self.observation_space = spaces.MultiDiscrete(
            [self._bin_values] * self._bin_num
        )
        self.observation_space_n = self._bin_values**self._bin_num
        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
            (1.0, 0.0),  # soft right
            (0.0, 1.0),  # soft left
        ]
        self.action_space = spaces.Discrete(len(self.actions))

    def _encode_observation(
        self, proximity_values, light_values, position, orientation
    ):
        bins = []
        values = [
            proximity_values[0],
            proximity_values[1],
            proximity_values[7],
        ]
        for val in values:
            if val < 0.1:
                bins.append(0)
            elif val < 0.3:
                bins.append(1)
            elif val < 0.6:
                bins.append(2)
            else:
                bins.append(3)

        # Convert to unique state ID
        state = 0
        for i, b in enumerate(bins):
            state += b * (self._bin_values**i)
        return state

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
