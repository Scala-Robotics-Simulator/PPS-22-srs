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
        virtual_grid_size: int = 5,
    ) -> None:
        super().__init__(server_address, client_name)

        self.actions = [
            (1.0, 1.0),  # move forward
            (1.0, -1.0),  # rotate clockwise
            (-1.0, 1.0),  # rotate counterclockwise
            (1.0, 0.0),  # gentle right curve
            (0.0, 1.0),  # gentle left curve
        ]
        self.action_space = spaces.Discrete(len(self.actions))

        self.grid_size = grid_size
        self.orientation_bins = orientation_bins
        self.virtual_grid_size = virtual_grid_size

        self.observation_space = spaces.Box(
            low=0.0, high=1.0, shape=(35,), dtype=np.float32
        )

        self.visited_virtual = np.zeros(
            (virtual_grid_size, virtual_grid_size), dtype=bool
        )

    def _update_virtual_visited(self, position):
        """Update the virtual visited grid based on the agent's position"""
        x_real = position.x
        y_real = position.y

        i = int(
            np.clip(
                x_real / self.grid_size[0] * self.virtual_grid_size,
                0,
                self.virtual_grid_size - 1,
            )
        )
        j = int(
            np.clip(
                y_real / self.grid_size[1] * self.virtual_grid_size,
                0,
                self.virtual_grid_size - 1,
            )
        )

        self.visited_virtual[i, j] = True

    def _encode_observation(
        self, proximity_values, light_values, position, orientation, visited_positions
    ):
        """Encode observation and update visited virtual grid"""
        self._update_virtual_visited(position)

        orientation_sin = np.sin(np.radians(orientation))
        orientation_cos = np.cos(np.radians(orientation))

        return np.array(
            [orientation_sin, orientation_cos, *visited_positions, *proximity_values],
            dtype=np.float32,
        )

    def _decode_action(self, action) -> rl_pb2.ContinuousAction:
        """Decode discrete action to continuous action"""
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)

    def reset(self, seed: int = 42):
        """Reset environment and virtual visited grid"""
        self.visited_virtual = np.zeros(
            (self.virtual_grid_size, self.virtual_grid_size), dtype=bool
        )
        observations, infos = super().reset(seed)

        explored_ratio = np.sum(self.visited_virtual) / (self.virtual_grid_size**2)
        new_infos = {
            agent_id: {"explored_ratio": explored_ratio}
            for agent_id in observations.keys()
        }

        return observations, new_infos

    def step(self, actions: dict):
        """Perform environment step and update virtual visited grid"""
        observations, rewards, terminateds, truncateds, infos = super().step(actions)
        explored_ratio = np.sum(self.visited_virtual) / (self.virtual_grid_size**2)
        new_infos = {
            agent_id: {"explored_ratio": explored_ratio}
            for agent_id in observations.keys()
        }

        return observations, rewards, terminateds, truncateds, new_infos
