#!/usr/bin/env python3
"""
Headless training runner for Deep Q Learning.

How to run:
    python train-dqagent.py --neurons 64 32 --config obstacle-avoidance --env oa --port 50051 --episodes 20 --steps 50 --checkpoint-interval 2 --checkpoint-dir checkpoints/oa
All other possible configurations are visible below or can be shown with:
    python train-dqagent.py --help
"""

from __future__ import annotations

import sys

sys.path.append("..")

import argparse
import os
import time
from pathlib import Path

import nest_asyncio

from agent.scala_dqagent import DQAgent
from environment.deepqlearning.obstacle_avoidance_env import ObstacleAvoidanceEnv
from training.dqnetwork import DQNetwork
from training.multi_agent_dqlearning import DQLearning
from utils.log import Logger
from utils.reader import get_yaml_path, read_file

nest_asyncio.apply()


# Initialize logger
logger = Logger(__name__)

# -------- Defaults (single source of truth) --------
DEFAULTS = {
    "config_name": "phototaxis.yml",  # name only, resolved via get_yaml_path("resources","configurations", name)
    "config_root": ("resources", "configurations"),
    "server_host": "localhost",
    "port": 50051,
    "episodes": 10,
    "steps": 5000,
    "checkpoint_interval": 200,  # 0 disables periodic checkpoints
    "checkpoint_dir": None,  # inferred from config basename
    "client_name": "PhototaxisRLClient",
    "env": "phototaxis",
    "neurons": [64, 32],
    "epsilon_max": 1.0,
    "epsilon_min": 0.01,
    "gamma": 0.99,
    "replay_memory_max_size": 100000,
    "replay_memory_init_size": 1000,
    "batch_size": 64,
    "step_per_update": 4,
    "step_per_update_target_model": 8,
    "moving_avg_window_size": 20,
    "moving_avg_stop_thr": 100,
}
FIXED_AGENT_ID = "00000000-0000-0000-0000-000000000001"


def ensure_yml_suffix(name: str) -> str:
    """Allow passing 'phototaxis_dense' or 'phototaxis_dense.yml'."""
    return name if name.endswith((".yml", ".yaml")) else f"{name}.yml"


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Phototaxis Q-Learning trainer (headless). Uses config NAME, not path.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    p.add_argument(
        "--config",
        type=str,
        default=DEFAULTS["config_name"],
        help="Configuration NAME (e.g., 'phototaxis.yml' or 'phototaxis_dense'). "
        "Resolved via get_yaml_path('resources','configurations', NAME).",
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
        "--episodes",
        type=int,
        default=DEFAULTS["episodes"],
        help="Number of training episodes.",
    )
    p.add_argument(
        "--steps",
        type=int,
        default=DEFAULTS["steps"],
        help="Max steps per episode.",
    )
    p.add_argument(
        "--checkpoint-interval",
        type=int,
        default=DEFAULTS["checkpoint_interval"],
        help="Save checkpoint every N episodes (0 = disabled).",
    )
    p.add_argument(
        "--checkpoint-dir",
        type=str,
        default=DEFAULTS["checkpoint_dir"],
        help="Checkpoint base path (without suffix). If omitted, inferred from config name.",
    )
    p.add_argument(
        "--client-name",
        type=str,
        default=DEFAULTS["client_name"],
        help="Client name for the environment.",
        required=False,
    )
    p.add_argument(
        "--env",
        type=str,
        default=DEFAULTS["env"],
        help="Environmen to use (for observations and actions)",
    )
    p.add_argument(
        "--neurons",
        type=int,
        nargs="+",
        default=DEFAULTS["neurons"],
        help="Number of neurons for the hidden layers of the network (formatted like 64 32)",
    )
    p.add_argument(
        "--epsilon-max",
        type=float,
        default=DEFAULTS["epsilon_max"],
        help="Maximum exploration rate (epsilon).",
        required=False,
    )
    p.add_argument(
        "--epsilon-min",
        type=float,
        default=DEFAULTS["epsilon_min"],
        help="Minimum exploration rate (epsilon).",
        required=False,
    )
    p.add_argument(
        "--gamma",
        type=float,
        default=DEFAULTS["gamma"],
        help="Discount factor (gamma).",
        required=False,
    )
    p.add_argument(
        "--replay-memory-max-size",
        type=int,
        default=DEFAULTS["replay_memory_max_size"],
        help="Maximum size of the replay memory.",
        required=False,
    )
    p.add_argument(
        "--replay-memory-init-size",
        type=int,
        default=DEFAULTS["replay_memory_init_size"],
        help="Initial size of the replay memory before training starts.",
        required=False,
    )
    p.add_argument(
        "--batch-size",
        type=int,
        default=DEFAULTS["batch_size"],
        help="Batch size for training.",
        required=False,
    )
    p.add_argument(
        "--step-per-update",
        type=int,
        default=DEFAULTS["step_per_update"],
        help="Number of steps between each network update.",
        required=False,
    )
    p.add_argument(
        "--step-per-update-target-model",
        type=int,
        default=DEFAULTS["step_per_update_target_model"],
        help="Number of steps between each target network update.",
        required=False,
    )
    p.add_argument(
        "--moving-avg-window-size",
        type=int,
        default=DEFAULTS["moving_avg_window_size"],
        help="Window size for moving average of rewards.",
        required=False,
    )
    p.add_argument(
        "--moving-avg-stop-thr",
        type=float,
        default=DEFAULTS["moving_avg_stop_thr"],
        help="Moving average reward threshold to stop training.",
        required=False,
    )
    return p.parse_args()


def resolve_config_path(config_name: str) -> str:
    """Resolve config NAME to a full path using get_yaml_path(resources, configurations, NAME)."""
    name = ensure_yml_suffix(config_name)
    base_a, base_b = DEFAULTS["config_root"]
    return get_yaml_path(base_a, base_b, name)


def resolve_env(env_name: str, server_address: str, client_name: str):
    match env_name:
        # case "phototaxis":
        #     return PhototaxisEnv(server_address, client_name)
        case "oa":
            return ObstacleAvoidanceEnv(server_address, client_name)
        case _:
            logger.error("Environment not found")
            exit(1)


def infer_checkpoint_base(config_path: str, explicit_dir: str | None) -> str:
    if explicit_dir:
        return explicit_dir.rstrip("/")
    stem = Path(config_path).stem  # e.g., 'phototaxis_dense'
    return f"checkpoints/{stem}"


def print_effective_config(
    args: argparse.Namespace, config_path: str, checkpoint_base: str
) -> None:
    logger.info("== Effective settings ==========")
    logger.info(f"  config_name                 : {args.config}")
    logger.info(f"  resolved_config             : {config_path}")
    logger.info(f"  server                      : {args.server_host}:{args.port}")
    logger.info(f"  client_name                 : {args.client_name}")
    logger.info(f"  episodes                    : {args.episodes}")
    logger.info(f"  steps/episode               : {args.steps}")
    logger.info(f"  checkpoint_interval         : {args.checkpoint_interval}")
    logger.info(f"  checkpoint_base             : {checkpoint_base}")
    logger.info(f"  env                         : {args.env}")
    logger.info(f"  neurons                     : {args.neurons}")
    logger.info(f"  epsilon_max                 : {args.epsilon_max}")
    logger.info(f"  epsilon_min                 : {args.epsilon_min}")
    logger.info(f"  gamma                       : {args.gamma}")
    logger.info(f"  replay_memory_max_size      : {args.replay_memory_max_size}")
    logger.info(f"  replay_memory_init_size     : {args.replay_memory_init_size}")
    logger.info(f"  batch_size                  : {args.batch_size}")
    logger.info(f"  step_per_update             : {args.step_per_update}")
    logger.info(f"  step_per_update_target_model: {args.step_per_update_target_model}")
    logger.info(f"  moving_avg_window_size      : {args.moving_avg_window_size}")
    logger.info(f"  moving_avg_stop_thr         : {args.moving_avg_stop_thr}")
    logger.info("================================\n")


def main() -> None:
    args = parse_args()

    # Resolve config NAME -> full path and load YAML
    config_path = resolve_config_path(args.config)
    config = read_file(config_path)
    if config is None:
        raise RuntimeError(f"Failed to read YAML config from: {config_path}")

    # Build server address
    server_address = f"{args.server_host}:{args.port}"

    # Init environment
    env = resolve_env(args.env, server_address, args.client_name)
    env.connect_to_client()
    env.init(config)

    action_net = DQNetwork(
        env.observation_space.shape,
        args.neurons,
        env.action_space.n,
        summary=False,
    )
    target_net = DQNetwork(
        env.observation_space.shape,
        args.neurons,
        env.action_space.n,
        summary=False,
    )

    # Agent(s)
    agent = DQAgent(
        env,
        agent_id=FIXED_AGENT_ID,
        action_model=action_net,
        target_model=target_net,
        epsilon_max=args.epsilon_max,
        epsilon_min=args.epsilon_min,
        gamma=args.gamma,
        replay_memory_max_size=args.replay_memory_max_size,
        replay_memory_init_size=args.replay_memory_init_size,
        batch_size=args.batch_size,
        step_per_update=args.step_per_update,
        step_per_update_target_model=args.step_per_update_target_model,
        moving_avg_window_size=args.moving_avg_window_size,
        moving_avg_stop_thr=args.moving_avg_stop_thr,
        episode_max_steps=args.steps,
        episodes=args.episodes,
    )

    train_start_time = time.time()

    trainer = DQLearning(
        env,
        [agent],
        episode_count=args.episodes,
        episode_max_steps=args.steps,
    )

    # # Compute checkpoint base & show effective settings
    checkpoint_base = infer_checkpoint_base(config_path, args.checkpoint_dir)
    print_effective_config(args, config_path, checkpoint_base)

    os.makedirs(os.path.dirname(checkpoint_base) or ".", exist_ok=True)

    # Train
    _ = trainer.simple_dqn_training(
        checkpoint_interval=args.checkpoint_interval, checkpoint_base=checkpoint_base
    )

    train_finish_time = time.time()
    train_elapsed_time = train_finish_time - train_start_time
    train_avg_episode_time = train_elapsed_time / args.episodes

    logger.info(
        f"Train time: {train_elapsed_time / 60.0:.1f}m [{train_avg_episode_time:.1f}s]"
    )


if __name__ == "__main__":
    main()
