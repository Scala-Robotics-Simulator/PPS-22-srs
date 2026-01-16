import gymnasium.spaces as spaces
import numpy as np

import rl_pb2
from environment.abstract_env import AbstractEnv
from utils.log import Logger

logger = Logger(__name__)


class ExplorationEnv(AbstractEnv):
    """Custom environment for Q-Learning Exploration via gRPC"""

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
        logger.info(self.action_space)
        self.grid_size = grid_size
        self.orientation_bins = orientation_bins
        self.observation_space_n = (
            self.grid_size[0] * self.grid_size[1] * self.orientation_bins
        )
        self.observation_space = spaces.Discrete(self.observation_space_n)
        logger.info(self.observation_space)
        self.visited = None
        self.total_cells = grid_size[0] * grid_size[1]

    def _encode_observation(
        self, proximity_values, light_values, position, orientation, visited_positions
    ) -> int:
        """Encode the agent's position and orientation into a discrete state."""
        orientation_step = 360.0 / self.orientation_bins
        orientation_idx = int((orientation % 360) / orientation_step)

        x = int(np.clip(position.x, 0, self.grid_size[0] - 1))
        y = int(np.clip(position.y, 0, self.grid_size[1] - 1))

        state = x
        state += y * self.grid_size[0]
        state += orientation_idx * self.grid_size[0] * self.grid_size[1]
        return state

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        """Decode the discrete action into continuous wheel speeds."""
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)

    def _decode_position(self, state):
        """Decode the discrete state into the agent's (x, y) position."""
        x = state % self.grid_size[0]
        y = (state // self.grid_size[0]) % self.grid_size[1]
        return x, y

    def reset(self, seed: int = 42):
        """Reset the environment and visited grid."""
        observations, infos = super().reset(seed)
        self.visited = np.zeros(self.grid_size, dtype=bool)
        for _agent_id, obs in observations.items():
            x, y = self._decode_position(obs)
            self.visited[x, y] = True
        explored_ratio = self.visited.sum() / self.total_cells
        new_infos = {
            agent_id: {"explored_ratio": explored_ratio}
            for agent_id in observations.keys()
        }
        return observations, new_infos

    def step(self, actions: dict):
        """Take a step in the environment and update visited grid."""
        observations, rewards, terminateds, truncateds, infos = super().step(actions)
        for _agent_id, obs in observations.items():
            x, y = self._decode_position(obs)
            self.visited[x, y] = True
        explored_ratio = self.visited.sum() / self.total_cells
        new_infos = {
            agent_id: {"explored_ratio": explored_ratio}
            for agent_id in observations.keys()
        }
        return observations, rewards, terminateds, truncateds, new_infos
