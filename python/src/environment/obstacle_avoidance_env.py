import gymnasium.spaces as spaces

import rl_pb2
from environment.abstract_env import AbstractEnv
from proto.rl_client import RLClient
from utils.log import Logger

logger = Logger(__name__)


class ObstacleAvoidanceEnv(AbstractEnv):
    """Custom environment class for RL interaction via gRPC"""

    def __init__(self, server_address, client_name) -> None:
        super().__init__(server_address, client_name)
        self.observation_space = spaces.MultiDiscrete([3] * 8)
        self.observation_space_n = 3**8
        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
        ]
        self.action_space = spaces.Discrete(len(self.actions))

    def _encode_observation(self, proximity_values, _):
        bins = []
        for val in proximity_values:
            if val < 0.02:
                bins.append(0)
            elif val < 0.2:
                bins.append(1)
            else:
                bins.append(2)

        # Convert to unique state ID
        state = 0
        for i, b in enumerate(bins):
            state += b * (3**i)
        return state

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
