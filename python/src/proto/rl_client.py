import asyncio
import io

import grpc
import numpy as np
from PIL import Image

import proto.rl_pb2 as rl_pb2
import proto.rl_pb2_grpc as rl_pb2_grpc
from utils.log import Logger

logger = Logger(__name__)


class RLClient:
    """Client for bidirectional RL communication with gRPC server"""

    def __init__(self, server_address: str, client_name: str):
        self.server_address = server_address
        self.client_name = client_name
        self.channel = None
        self.stub = None

    async def connect(self):
        """Establish connection to the server"""
        self.channel = grpc.aio.insecure_channel(self.server_address)
        self.stub = rl_pb2_grpc.RLStub(self.channel)

        # Test connection
        await asyncio.wait_for(self.channel.channel_ready(), timeout=5.0)
        logger.info(f"✓ Connected to {self.server_address}\n")

    async def close(self):
        """Close the connection"""
        if self.channel:
            logger.info(f"✓ Closed connection to {self.server_address}")
            await self.channel.close()

    async def init(self, yaml_config: str) -> tuple[bool, str | None]:
        """
        Initialize the simulation environment.
        Args:
            yaml_config: YAML configuration string
        Returns:
            Tuple of (success, error_message)
        """
        request = rl_pb2.InitRequest(config=yaml_config)
        response = await self.stub.Init(request)
        if response.ok:
            logger.info("✓ Initialization successful")
        else:
            logger.warning(f"✗ Initialization failed: {response.message}")
        return response.ok, response.message

    async def step(self, actions: dict[str, dict]) -> tuple[
        dict[str, dict],
        dict[str, float],
        dict[str, bool],
        dict[str, bool],
        dict[str, str],
    ]:
        """
        Take a simulation step with the provided actions.
        Args:
            actions: Dictionary mapping agent IDs to their action dictionaries
        Returns:
            Tuple of (observations, rewards, terminateds, truncateds, infos)
        """
        request = rl_pb2.StepRequest(actions=actions)
        response = await self.stub.Step(request)
        logger.debug(
            f"✓ Step taken: observations={response.observations}, rewards={response.rewards}, terminateds={response.terminateds}, truncateds={response.truncateds}, infos={response.infos}"
        )
        return (
            response.observations,
            response.rewards,
            response.terminateds,
            response.truncateds,
            response.infos,
        )

    async def render(self, width: int = 800, height: int = 600) -> np.ndarray:
        """
        Render the current environment state.
        Args:
            width: Image width in pixels
            height: Image height in pixels
        Returns:
            Numpy array of the rendered RGB image
        """
        request = rl_pb2.RenderRequest(width=width, height=height)
        response = await self.stub.Render(request)
        logger.debug(
            f"✓ Rendered {response.format} image: {response.width}x{response.height}"
        )

        image = Image.open(io.BytesIO(response.image))
        if image.mode != "RGB":
            image = image.convert("RGB")
        return np.array(image)

    async def reset(self, seed: int) -> tuple[dict[str, dict], dict[str, str]]:
        """
        Reset the simulation environment with a specific seed.
        Args:
            seed: integer seed for reproducibility
        Returns:
            Tuple of (observations, infos)
        """
        request = rl_pb2.ResetRequest(seed=seed)
        response = await self.stub.Reset(request)
        logger.debug(
            f"✓ Environment reset: observations={response.observations}, infos={response.infos}"
        )
        return response.observations, response.infos
