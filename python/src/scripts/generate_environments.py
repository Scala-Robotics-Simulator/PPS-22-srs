#!/usr/bin/env python3
"""
Generator of random environments

How to run:
`
python3 generate_environments.py --num 10 --config-root resources generated obstacle-avoidance --width 10 --height 10 --obstacle-min-num 4 --obstacle-max-num 15 --obstacle-min-size 0.5 --obstacle-max-size 8.0 --light-min-num 0 --light-max-num 0 --agent-num 1
`
"""

from __future__ import annotations

import sys

sys.path.append("..")

import argparse
import os
from pathlib import Path

import nest_asyncio

from environment.qlearning.obstacle_avoidance_env import ObstacleAvoidanceEnv
from scripts.lib.environment_generator import generate_multiple_environments
from utils.log import Logger

nest_asyncio.apply()

# Initialize logger
logger = Logger(__name__)

# -------- Defaults (single source of truth) --------
DEFAULTS = {
    "server_host": "localhost",
    "port": 50051,
    "client_name": "EnvGeneratorClient",
    "num": 5,
    "width": 10,
    "height": 10,
    "obstacle_min_num": 4,
    "obstacle_max_num": 15,
    "obstacle_min_size": 0.5,
    "obstacle_max_size": 8.0,
    "light_min_num": 0,
    "light_max_num": 0,
    "agent_num": 1,
}
FIXED_AGENT_ID = "00000000-0000-0000-0000-000000000001"


def ensure_yml_suffix(name: str) -> str:
    """Allow passing 'phototaxis_dense' or 'phototaxis_dense.yml'."""
    return name if name.endswith((".yml", ".yaml")) else f"{name}.yml"


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Random environment generator",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    p.add_argument(
        "--config-root",
        type=str,
        nargs="+",
        help="Path components to the configuration root directory.",
    )
    p.add_argument(
        "--server-host",
        type=str,
        default=DEFAULTS["server_host"],
        help="Server hostname or IP for the proto server.",
        required=False,
    )
    p.add_argument(
        "--port",
        type=int,
        default=DEFAULTS["port"],
        help="Server port (e.g., 5051, 5052, 5053).",
    )
    p.add_argument(
        "--client-name",
        type=str,
        default=DEFAULTS["client_name"],
        help="Client name for the environment.",
        required=False,
    )
    p.add_argument(
        "--num",
        type=int,
        default=DEFAULTS["num"],
        help="Number of environments to generate",
        required=False,
    )
    p.add_argument(
        "--width",
        type=int,
        default=DEFAULTS["width"],
        help="Width of the environment.",
        required=False,
    )
    p.add_argument(
        "--height",
        type=int,
        default=DEFAULTS["height"],
        help="Height of the environment.",
        required=False,
    )
    p.add_argument(
        "--obstacle-min-num",
        type=int,
        default=DEFAULTS["obstacle_min_num"],
        help="Minimum number of obstacles to generate.",
    )
    p.add_argument(
        "--obstacle-max-num",
        type=int,
        default=DEFAULTS["obstacle_max_num"],
        help="Maximum number of obstacles to generate.",
    )
    p.add_argument(
        "--obstacle-min-size",
        type=float,
        default=DEFAULTS["obstacle_min_size"],
        help="Minimum size of obstacles.",
    )
    p.add_argument(
        "--obstacle-max-size",
        type=float,
        default=DEFAULTS["obstacle_max_size"],
        help="Maximum size of obstacles.",
    )
    p.add_argument(
        "--light-min-num",
        type=int,
        default=DEFAULTS["light_min_num"],
        help="Minimum number of lights to generate.",
    )
    p.add_argument(
        "--light-max-num",
        type=int,
        default=DEFAULTS["light_max_num"],
        help="Maximum number of lights to generate.",
    )
    p.add_argument(
        "--agent-num",
        type=int,
        default=DEFAULTS["agent_num"],
        help="Number of agents in the environment.",
    )
    return p.parse_args()


def print_effective_config(args: argparse.Namespace) -> None:
    logger.info("== Effective settings ==========")
    logger.info(f"  config_root                 : {args.config_root}")
    logger.info(f"  server                      : {args.server_host}:{args.port}")
    logger.info(f"  client_name                 : {args.client_name}")
    logger.info(f"  num                         : {args.num}")
    logger.info(f"  width                       : {args.width}")
    logger.info(f"  height                      : {args.height}")
    logger.info(f"  obstacle_min_num            : {args.obstacle_min_num}")
    logger.info(f"  obstacle_max_num            : {args.obstacle_max_num}")
    logger.info(f"  obstacle_min_size           : {args.obstacle_min_size}")
    logger.info(f"  obstacle_max_size           : {args.obstacle_max_size}")
    logger.info(f"  light_min_num               : {args.light_min_num}")
    logger.info(f"  light_max_num               : {args.light_max_num}")
    logger.info(f"  agent_num                   : {args.agent_num}")
    logger.info("================================\n")


def main() -> None:
    args = parse_args()

    # Build server address
    server_address = f"{args.server_host}:{args.port}"

    # Init environment
    env = ObstacleAvoidanceEnv(server_address, args.client_name)
    env.connect_to_client()

    print_effective_config(args)

    configs_path = Path(*args.config_root)
    os.makedirs(os.path.dirname(configs_path), exist_ok=True)

    config = {
        "env_width": args.width,
        "env_height": args.height,
        "min_obstacles": args.obstacle_min_num,
        "max_obstacles": args.obstacle_max_num,
        "min_obstacle_width": args.obstacle_min_size,
        "max_obstacle_width": args.obstacle_max_size,
        "min_obstacle_height": args.obstacle_min_size,
        "max_obstacle_height": args.obstacle_max_size,
        "min_lights": args.light_min_num,
        "max_lights": args.light_max_num,
        "agent_num": args.agent_num,
    }

    files = generate_multiple_environments(
        env=env,
        num_environments=args.num,
        generator_config=config,
        output_dir=configs_path,
        max_retries=None,
    )
    logger.info(f"\nSuccessfully generated {len(files)} valid environment files")


if __name__ == "__main__":
    main()
