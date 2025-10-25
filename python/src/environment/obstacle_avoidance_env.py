import asyncio

import grpc
import gymnasium.spaces as spaces
import numpy as np

import rl_pb2
from proto.rl_client import RLClient
from utils.log import Logger

logger = Logger(__name__)


class ObstacleAvoidanceEnv:
    """Custom environment class for RL interaction via gRPC"""

    def __init__(self, server_address, client_name) -> None:
        self.client = RLClient(server_address, client_name)
        self.observation_space = spaces.MultiDiscrete([8, 11])
        self.observation_space_n = 8 * 11
        self.actions = [
            (1.0, 1.0),  # forward
            (1.0, -1.0),  # left
            (-1.0, 1.0),  # right
        ]
        self.action_space = spaces.Discrete(len(self.actions))

    async def init_client(self):
        """Initialize the RL client and connect to the server

        Parameters
        ----------
        server_address : str
            The address of the gRPC server.
        client_name : str
            The name of the client.
        """
        try:
            await self.client.connect()
        except asyncio.TimeoutError:
            logger.info("✗ Connection timeout")
        except grpc.aio.AioRpcError as e:
            logger.info(f"✗ RPC failed: {e.code()}: {e.details()}")
        except Exception as e:
            logger.info(f"✗ Error: {e}")

    async def init(self, yaml_config: str):
        """Initialize the environment with the given YAML configuration

        Parameters
        ----------
        yaml_config : str
            The YAML configuration string.
        """
        await self.client.init(yaml_config)

    def _extract_state(self, proximity_values):
        # find max values and indices
        idx1 = int(np.argmin(proximity_values))
        val1 = round(float(proximity_values[idx1]), 1)

        return self._encode_observation(idx1, val1)

    def _encode_observation(self, idx1, val1):
        v1 = int(val1 * 10)
        return idx1 * 11 + v1

    def _encode_observations(self, observations):
        return {
            k: self._extract_state(v.proximity_values) for k, v in observations.items()
        }

    async def reset(self, seed: int = 42) -> tuple[dict, dict]:
        """Reset the environment to an initial state

        Parameters
        ----------
        seed : int
            The seed for random number generation.
        """
        observations, infos = await self.client.reset(seed)
        return self._encode_observations(observations), infos

    def _decode_action(self, action):
        left, right = self.actions[action]
        return rl_pb2.ContinuousAction(left_wheel=left, right_wheel=right)

    def _decode_actions(self, actions):
        return {k: self._decode_action(v) for k, v in actions.items()}

    async def step(self, actions: dict) -> tuple[dict, dict, dict, dict, dict]:
        """Take a step in the environment with the given actions

        Parameters
        ----------
        actions : dict
            A dictionary mapping agent IDs to their respective actions.

        Returns
        -------
        tuple[dict, dict, dict, dict, dict]
            A tuple containing observations, rewards, terminateds, truncateds, and infos.
        """
        actions = self._decode_actions(actions)

        observations, rewards, terminateds, truncateds, infos = await self.client.step(
            actions
        )
        return (
            self._encode_observations(observations),
            rewards,
            terminateds,
            truncateds,
            infos,
        )

    async def render(self, width: int = 800, height: int = 600) -> "np.ndarray":
        """Render the current state of the environment

        Parameters
        ----------
        width : int
            The width of the rendered image.
        height : int
            The height of the rendered image.

        Returns
        -------
        np.ndarray
            A numpy array representing the rendered RGB image.
        """
        return await self.client.render(width, height)

    async def close(self):
        """Close the environment and the client connection"""
        await self.client.close()
