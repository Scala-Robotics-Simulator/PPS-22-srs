import gymnasium.spaces as spaces
import numpy as np

import rl_pb2
from environment.abstract_env import AbstractEnv
from utils.log import Logger

logger = Logger(__name__)


class PhototaxisEnv(AbstractEnv):
    """Custom environment for deep q learning phototaxis via gRPC"""

    def __init__(self, server_address, client_name) -> None:
        super().__init__(server_address, client_name)

        # observation space
        self.observation_space = spaces.Box(
            low=0.0, high=1.0, shape=(16,), dtype=np.float32
        )
        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
            (1.0, 0.0),  # soft right
            (0.0, 1.0),  # soft left
        ]

        self.action_space = spaces.Discrete(len(self.actions))

    def _encode_observation(
        self,
        proximity_values,
        light_values,
        position=None,
        orientation=None,
        visited_positions=None,
    ):
        prox = np.array(
            proximity_values[:8] if proximity_values else [0.0] * 8, dtype=np.float32
        )
        light = np.array(
            light_values[:8] if light_values else [0.0] * 8, dtype=np.float32
        )

        # pad if needed
        if len(prox) < 8:
            prox = np.pad(prox, (0, 8 - len(prox)), constant_values=1.0)
        if len(light) < 8:
            light = np.pad(light, (0, 8 - len(light)), constant_values=0.0)

        return np.concatenate([prox, light])

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
