import asyncio
import logging
import grpc

from proto.rl_client import RLClient

logging.basicConfig(level=logging.INFO, format="%(message)s")
logger = logging.getLogger(__name__)


async def main():
    """Main entry point"""
    server_address = "localhost:50051"
    client_name = "RLClient"
    # ping_count = 5
    # ping_interval = 1.0  # seconds

    client = RLClient(server_address, client_name)

    try:
        await client.connect()
        # await client.run() # (count=ping_count, interval=ping_interval)
    except asyncio.TimeoutError:
        logger.info(f"✗ Connection timeout - server at {server_address} not responding")
    except grpc.aio.AioRpcError as e:
        logger.info(f"✗ RPC failed: {e.code()}: {e.details()}")
    except Exception as e:
        logger.info(f"✗ Error: {e}")
    finally:
        await client.close()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("\n\n✗ Interrupted by user")