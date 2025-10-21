import asyncio
import logging

import grpc
import rl_pb2
import rl_pb2_grpc

logging.basicConfig(level=logging.INFO, format="%(message)s")

logger = logging.getLogger(__name__)


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
            await self.channel.close()