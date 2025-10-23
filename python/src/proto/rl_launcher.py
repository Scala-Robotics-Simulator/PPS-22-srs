import asyncio
import logging

import numpy as np
import pygame
from python.src.environment.env import Env
from python.src.utils.reader import get_yaml_path, read_file

logging.basicConfig(level=logging.INFO, format="%(message)s")
logger = logging.getLogger(__name__)


async def main():
    """Main entry point"""
    server_address = "localhost:50051"
    client_name = "RLClient"

    env = Env()
    await env.init_client(server_address, client_name)

    config_path = get_yaml_path("resources", "configurations", "phototaxis.yml")
    config = read_file(config_path)
    logger.info(config)

    _ = await env.init(yaml_config=config)

    observations, infos = await env.reset(seed=42)

    rgb_array = await env.render()

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
            },
        }

        observations, rewards, terminateds, truncateds, infos = await env.step(
            actions=actions
        )

        rgb_array = await env.render()

        surface = pygame.surfarray.make_surface(np.transpose(rgb_array, (1, 0, 2)))
        screen.blit(surface, (0, 0))
        pygame.display.flip()

        clock.tick(30)

        if not running:
            break
    pygame.quit()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("\n\n✗ Interrupted by user")
