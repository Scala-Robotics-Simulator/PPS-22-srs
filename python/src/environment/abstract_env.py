import asyncio
from abc import ABC, abstractmethod

import grpc
import numpy as np

from proto.rl_client import RLClient
from utils.log import Logger

logger = Logger(__name__)


class AbstractEnv(ABC):
    """
    Custom environment class for RL interaction via gRPC with abstract methods for encoding observations and decoding actions.

    Parameters
    ----------
    server_address : str
        The address of the gRPC server.
    client_name : str
        The name of the client.

    Attributes
    ----------
    client : RLClient
        The RL client for gRPC communication.
    render_mode : str
        The render mode for the environment.
    loop : asyncio.AbstractEventLoop
        The event loop for running async operations.
    """

    def __init__(self, server_address, client_name) -> None:
        self.client = RLClient(server_address, client_name)
        self.render_mode = "rgb_array"
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)

    def _run_async(self, coro):
        """Helper method to run async coroutines synchronously"""
        return self.loop.run_until_complete(coro)

    @abstractmethod
    def _encode_observation(self, proximity_values, light_values):
        """Encode the observation from proximity and light values"""
        pass

    @abstractmethod
    def _decode_action(self, action):
        """Decode the action into the appropriate format"""
        pass

    def _encode_observations(self, observations):
        """Encode multiple observations"""
        return {
            k: self._encode_observation(v.proximity_values, v.light_values)
            for k, v in observations.items()
        }

    def _decode_actions(self, actions):
        """Decode multiple actions"""
        return {k: self._decode_action(v) for k, v in actions.items()}

    def connect_to_client(self):
        """Initialize the RL client and connect to the server"""
        try:
            self._run_async(self.client.connect())
        except asyncio.TimeoutError:
            logger.info(
                f"✗ Connection timeout - server at {self.client.server_address} not responding"
            )
        except grpc.aio.AioRpcError as e:
            logger.info(f"✗ RPC failed: {e.code()}: {e.details()}")
        except Exception as e:
            logger.info(f"✗ Error: {e}")

    def init(self, yaml_config: str):
        """Initialize the environment with the given YAML configuration

        Parameters
        ----------
        yaml_config : str
            The YAML configuration string.
        """
        self._run_async(self.client.init(yaml_config))

    def step(self, actions: dict) -> tuple[dict, dict, dict, dict, dict]:
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

        observations, rewards, terminateds, truncateds, infos = self._run_async(
            self.client.step(actions)
        )
        return (
            self._encode_observations(observations),
            rewards,
            terminateds,
            truncateds,
            infos,
        )

    def render(self, width: int = 800, height: int = 600) -> np.ndarray:
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
        return self._run_async(self.client.render(width, height))

    def reset(self, seed: int = 42) -> tuple[dict, dict]:
        """Reset the environment to an initial state

        Parameters
        ----------
        seed : int
            The seed for random number generation.
        """
        observations, infos = self._run_async(self.client.reset(seed))
        return self._encode_observations(observations), infos

    def close(self):
        """Close the environment and the client connection"""
        self._run_async(self.client.close())
        self.loop.close()
