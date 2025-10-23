import asyncio
import logging

import grpc
import numpy as np
import pygame
from python.src.utils.reader import get_yaml_path, read_file
from rl_client import RLClient

logging.basicConfig(level=logging.INFO, format="%(message)s")
logger = logging.getLogger(__name__)


async def main():
    """Main entry point"""
    server_address = "localhost:50051"
    client_name = "RLClient"

    client = RLClient(server_address, client_name)

    try:
        await client.connect()

        config_path = get_yaml_path(
            "resources", "configurations", "phototaxis.yml"
        )
        config = read_file(config_path)
        logger.info(config)

        _ = await client.init(yaml_config=config)

        observations, infos = await client.reset(seed=42)

        rgb_array = await client.render()

        # TODO: delete, just for testing display
        pygame.init()
        screen = pygame.display.set_mode((800, 600))
        pygame.display.set_caption("DQN Agent Playing")
        clock = pygame.time.Clock()
        running = True

        for _ in range(1000):
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False

            actions = {
                "00000000-0000-0000-0000-000000000001": {
                    "left_wheel": 1.0,
                    "right_wheel": 1.0,
                },
                "00000000-0000-0000-0000-000000000002": {
                    "left_wheel": -1.0,
                    "right_wheel": -1.0,
                }
            }

            observations, rewards, terminateds, truncateds, infos = await client.step(
                actions=actions
            )

            rgb_array = await client.render()

            surface = pygame.surfarray.make_surface(
                np.transpose(rgb_array, (1, 0, 2))
            )
            screen.blit(surface, (0, 0))
            pygame.display.flip()

            clock.tick(30)

            if not running:
                break
        pygame.quit()

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
