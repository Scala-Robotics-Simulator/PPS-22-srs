import gymnasium.spaces as spaces

import rl_pb2
from environment.abstract_env import AbstractEnv
from utils.log import Logger

logger = Logger(__name__)


class PhototaxisEnv(AbstractEnv):
    """Custom environment class for a phototaxis RL task via gRPC.
    The agent learns to navigate towards light sources using light sensors.
    """

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

    def _encode_observation(self, proximity_values, light_values):
        bins = []
        for val in light_values:
            if val < 0.1:
                bins.append(0)  # No/weak light
            elif val < 0.5:
                bins.append(1)  # Medium light
            else:
                bins.append(2)  # Strong light

        # Convert to unique state ID
        state = 0
        for i, b in enumerate(bins):
            state += b * (3**i)
        return state

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)
