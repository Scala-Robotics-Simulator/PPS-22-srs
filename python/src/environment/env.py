import asyncio
from typing import TYPE_CHECKING

import grpc
from python.src.proto.rl_client import RLClient
from python.src.utils.log import Logger

if TYPE_CHECKING:
    import numpy as np

logger = Logger(__name__)


class Env:
    """Custom environment class for RL interaction via gRPC"""

    def __init__(self) -> None:
        self.client = None
        self.action_space = None
        self.observation_space = None
        self.metadata = None
        self.render_mode = None
        self.spec = None
        self.unwrapper = None
        self.np_random = None
        self.np_random_seed = None

    async def init_client(self, server_address, client_name):
        """Initialize the RL client and connect to the server

        Parameters
        ----------
        server_address : str
            The address of the gRPC server.
        client_name : str
            The name of the client.
        """
        self.client = RLClient(server_address, client_name)
        try:
            await self.client.connect()
        except asyncio.TimeoutError:
            logger.info(
                f"✗ Connection timeout - server at {server_address} not responding"
            )
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

    async def reset(self, seed: int) -> tuple[dict, dict]:
        """Reset the environment to an initial state

        Parameters
        ----------
        seed : int
            The seed for random number generation.
        """
        return await self.client.reset(seed)

    async def step(self, actions: dict) -> tuple[dict, dict, dict, dict, dict]:
        """Take a step in the environment with the given actions

        Parameters
        ----------
        actions : dict
            A dictionary mapping agent IDs to their respective action dictionaries.

        Returns
        -------
        tuple[dict, dict, dict, dict, dict]
            A tuple containing observations, rewards, terminateds, truncateds, and infos.
        """
        return await self.client.step(actions)

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
