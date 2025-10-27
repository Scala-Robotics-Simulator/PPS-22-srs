import gymnasium.spaces as spaces
import numpy as np
from python.src.environment.abstract_env import AbstractEnv

import rl_pb2


class ExplorationEnv(AbstractEnv):
    """Custom environment class for RL interaction via gRPC for exploration tasks implementing observation encoding and
    action decoding."""

    def __init__(self, server_address, client_name) -> None:
        super().__init__(server_address, client_name)
        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
        ]
        proximity_sensors_n = 8
        values_per_sensor = 11  # 0.0 to 1.0 in steps of 0.1
        self.action_space = spaces.Discrete(len(self.actions))
        self.observation_space_n = proximity_sensors_n * values_per_sensor
        self.observation_space = spaces.Discrete(self.observation_space_n)

    def _encode_observation(self, proximity_values, light_values=None) -> int:
        idx1 = int(np.argmin(proximity_values))
        val1 = round(float(proximity_values[idx1]), 1)
        v1 = int(val1 * 10)
        return idx1 * 11 + v1

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
