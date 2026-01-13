import gymnasium.spaces as spaces
import numpy as np

import rl_pb2
from environment.abstract_env import AbstractEnv
from utils.log import Logger

logger = Logger(__name__)


class ObstacleAvoidanceEnv(AbstractEnv):
    """Custom environment for deep q learning obstacle avoidance via gRPC"""

    def __init__(self, server_address, client_name) -> None:
        super().__init__(server_address, client_name)

        self.observation_space = spaces.Box(
            low=0.0, high=1.0, shape=(8,), dtype=np.float32
        )

        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
            (1.0, 0.0),
            (0.0, 1.0),
        ]

        self.action_space = spaces.Discrete(len(self.actions))

    def _encode_observation(
        self, proximity_values, light_values, position, orientation, visited_pos
    ):
        return np.array(proximity_values, dtype=np.float32)

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
