#!/usr/bin/env python3
"""
train.py — Headless training runner for Phototaxis Q-Learning.

Saved commands:
python3 train-qagent.py --config-root src scripts resources generated obstacle-avoidance --episodes 1000 --steps 5000 --checkpoint-interval 100 --checkpoint-dir src scripts resources generated obstacle-avoidance checkpoints --env oa
"""

from __future__ import annotations

import sys

sys.path.append("..")

import argparse
import os

import nest_asyncio
import numpy as np
from tqdm import trange

from agent.qagent import QAgent
from environment.qlearning.obstacle_avoidance_env import ObstacleAvoidanceEnv
from environment.qlearning.phototaxis_env import PhototaxisEnv
from utils.log import Logger
from utils.reader import get_yaml_path, read_file

nest_asyncio.apply()


# Initialize logger
logger = Logger(__name__)

# -------- Defaults (single source of truth) --------
DEFAULTS = {
    "config_root": ("resources", "configurations"),
    "server_host": "localhost",
    "port": 50051,
    "episodes": 10,
    "steps": 5000,
    "checkpoint_interval": 200,  # 0 disables periodic checkpoints
    "checkpoint_dir": None,  # inferred from config basename
    "load_checkpoint": None,
    "start_episode": 0,
    "client_name": "PhototaxisRLClient",
    "env": "phototaxis",
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
        "--config-root",
        type=str,
        nargs="*",
        default=DEFAULTS["config_root"],
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
        required=False,
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
        nargs="*",
        default=DEFAULTS["checkpoint_dir"],
        help="Checkpoint base path (without suffix). If omitted, inferred from config name.",
    )
    p.add_argument(
        "--load-checkpoint",
        type=str,
        default=DEFAULTS["load_checkpoint"],
        help="Optional path to load a previously saved agent (warm-start).",
        required=False,
    )
    p.add_argument(
        "--start-episode",
        type=int,
        default=DEFAULTS["start_episode"],
        help="Starting episode index (useful when resuming).",
        required=False,
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
    return p.parse_args()


def resolve_env(
    env_name: str, server_address: str, client_name: str
) -> PhototaxisEnv | ObstacleAvoidanceEnv:
    match env_name:
        case "phototaxis":
            return PhototaxisEnv(server_address, client_name)
        case "oa":
            return ObstacleAvoidanceEnv(server_address, client_name)
        case _:
            logger.error("Environment not found")
            exit(1)


def print_effective_config(
    args: argparse.Namespace, config_path: str, checkpoint_base: str
) -> None:
    logger.info("== Effective settings ==")
    logger.info(f"  resolved_config    : {config_path}")
    logger.info(f"  server             : {args.server_host}:{args.port}")
    logger.info(f"  client_name        : {args.client_name}")
    logger.info(f"  episodes           : {args.episodes}")
    logger.info(f"  steps/episode      : {args.steps}")
    logger.info(f"  checkpoint_interval: {args.checkpoint_interval}")
    logger.info(f"  checkpoint_base    : {checkpoint_base}")
    logger.info(f"  load_checkpoint    : {args.load_checkpoint or 'None'}")
    logger.info(f"  start_episode      : {args.start_episode}")
    logger.info(f"  env                : {args.env}")
    logger.info("========================\n")


def run_episodes(
    env: PhototaxisEnv | ObstacleAvoidanceEnv,
    configs: list[str],
    agents: dict[str, QAgent],
    agent_id: str,
    episode_count: int,
    episode_max_steps: int,
    checkpoint_interval: int | None,
    checkpoint_base: str,
    start_episode: int = 0,
    load_checkpoint: str | None = None,
) -> None:
    # Warm start
    if load_checkpoint:
        for a in agents.values():
            a.load(load_checkpoint)
        logger.info(f"[Warm Start] Loaded agent from: {load_checkpoint}")

    # Ensure checkpoint directory exists (always, for final save)
    os.makedirs(os.path.dirname(checkpoint_base) or ".", exist_ok=True)

    try:
        for ep_idx in trange(episode_count, desc="Training", unit="ep"):
            actual_episode = start_episode + ep_idx

            config = np.random.choice(configs)
            _ = env.init(config)
            obs, _ = env.reset()
            done = False
            step = 0
            total_reward = {agent_id: 0.0}

            while not done and step < episode_max_steps:
                actions = {
                    k: agents[k].choose_action(v, epsilon_greedy=True)
                    for k, v in obs.items()
                }

                next_obs, rewards, terminateds, truncateds, _ = env.step(actions)
                done = terminateds[agent_id] or truncateds[agent_id]

                for k in next_obs.keys():
                    agents[k].update_q(
                        obs[k], actions[k], rewards[k], next_obs[k], done
                    )
                    total_reward[k] += float(rewards[k])

                obs = next_obs
                step += 1

            for a in agents.values():
                a.decay_epsilon(actual_episode)

            if checkpoint_interval and checkpoint_interval > 0:
                if (ep_idx + 1) % checkpoint_interval == 0:
                    for _, a in agents.items():
                        save_path = f"{checkpoint_base}_ep{actual_episode + 1}"
                        a.save(save_path)
                    logger.info(
                        f"\n[Checkpoint] Saved at episode {actual_episode + 1} | Reward: {total_reward[agent_id]:.3f}"
                    )

    finally:
        for _, a in agents.items():
            final_path = f"{checkpoint_base}_final"
            a.save(final_path)
        logger.info("\n[Final Save] Training complete.")


def main() -> None:
    args = parse_args()

    config_path = get_yaml_path(*args.config_root)
    yml_files = list(config_path.glob("*.yml"))
    configs = [read_file(f) for f in yml_files]
    if len(configs) < 5:
        logger.error(
            f"Not enough configurations, you have {len(configs)} at least 5 needed"
        )
        exit(1)

    # Build server address
    server_address = f"{args.server_host}:{args.port}"

    # Init environment
    env = resolve_env(args.env, server_address, args.client_name)
    env.connect_to_client()

    # Agent(s)
    agent = QAgent(env, episodes=args.episodes)
    agents = {FIXED_AGENT_ID: agent}

    # Compute checkpoint base & show effective settings
    checkpoint_base = get_yaml_path(*args.checkpoint_dir)
    print_effective_config(args, config_path, checkpoint_base)

    # Train
    run_episodes(
        env=env,
        configs=configs,
        agents=agents,
        agent_id=FIXED_AGENT_ID,
        episode_count=args.episodes,
        episode_max_steps=args.steps,
        checkpoint_interval=args.checkpoint_interval,
        checkpoint_base=checkpoint_base,
        start_episode=args.start_episode,
        load_checkpoint=args.load_checkpoint,
    )

    # Quick stats
    logger.info(f"Q-table shape: {agent.Q.shape}")
    logger.info(f"Non-zero entries: {np.count_nonzero(agent.Q)}")
    logger.info(f"Q-table min/max: {agent.Q.min():.4f} / {agent.Q.max():.4f}")
    visited_states = np.where(np.any(agent.Q != 0, axis=1))[0]
    logger.info(f"States visited: {len(visited_states)} / {agent.Q.shape[0]}")


if __name__ == "__main__":
    main()
