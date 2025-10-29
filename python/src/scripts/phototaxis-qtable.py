#!/usr/bin/env python3
"""
train.py — Headless training runner for Phototaxis Q-Learning.

Saved commands:
    python .\phototaxis-qtable.py --config phototaxis --server-host localhost --port 50051 --episodes 2000 --steps 5000 --checkpoint-interval 200 --checkpoint-dir checkpoints\phototaxis --start-episode 0 --client-name PhototaxisRLClient1
    python .\phototaxis-qtable.py --config phototaxis_aggressive --server-host localhost --port 50052 --episodes 2000 --steps 5000 --checkpoint-interval 200 --checkpoint-dir checkpoints\phototaxis_aggressive --start-episode 0 --client-name PhototaxisRLClient2
    python .\phototaxis-qtable.py --config phototaxis_safety --server-host localhost --port 50053 --episodes 2000 --steps 5000 --checkpoint-interval 200 --checkpoint-dir checkpoints\phototaxis_safety --start-episode 0 --client-name PhototaxisRLClient3
    python .\phototaxis-qtable.py --config phototaxis_balanced --server-host localhost --port 50054 --episodes 2000 --steps 5000 --checkpoint-interval 200 --checkpoint-dir checkpoints\phototaxis_balanced --start-episode 0 --client-name PhototaxisRLClient4
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

import nest_asyncio
import numpy as np
from tqdm import trange

sys.path.append("..")

from agent.qagent import QAgent
from environment.qlearning.phototaxis_env import PhototaxisEnv
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
    "load_checkpoint": None,
    "start_episode": 0,
    "client_name": "PhototaxisRLClient",
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
        "--load-checkpoint",
        type=str,
        default=DEFAULTS["load_checkpoint"],
        help="Optional path to load a previously saved agent (warm-start).",
    )
    p.add_argument(
        "--start-episode",
        type=int,
        default=DEFAULTS["start_episode"],
        help="Starting episode index (useful when resuming).",
    )
    p.add_argument(
        "--client-name",
        type=str,
        default=DEFAULTS["client_name"],
        help="Client name for the environment.",
    )
    return p.parse_args()


def resolve_config_path(config_name: str) -> str:
    """Resolve config NAME to a full path using get_yaml_path(resources, configurations, NAME)."""
    name = ensure_yml_suffix(config_name)
    base_a, base_b = DEFAULTS["config_root"]
    return get_yaml_path(base_a, base_b, name)


def infer_checkpoint_base(config_path: str, explicit_dir: str | None) -> str:
    if explicit_dir:
        return explicit_dir.rstrip("/")
    stem = Path(config_path).stem  # e.g., 'phototaxis_dense'
    return f"checkpoints/{stem}"


def print_effective_config(
    args: argparse.Namespace, config_path: str, checkpoint_base: str
) -> None:
    logger.info("== Effective settings ==")
    logger.info(f"  config_name        : {args.config}")
    logger.info(f"  resolved_config    : {config_path}")
    logger.info(f"  server             : {args.server_host}:{args.port}")
    logger.info(f"  client_name        : {args.client_name}")
    logger.info(f"  episodes           : {args.episodes}")
    logger.info(f"  steps/episode      : {args.steps}")
    logger.info(f"  checkpoint_interval: {args.checkpoint_interval}")
    logger.info(f"  checkpoint_base    : {checkpoint_base}")
    logger.info(f"  load_checkpoint    : {args.load_checkpoint or 'None'}")
    logger.info(f"  start_episode      : {args.start_episode}")
    logger.info("========================\n")


def run_episodes(
    env: PhototaxisEnv,
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

    # Resolve config NAME -> full path and load YAML
    config_path = resolve_config_path(args.config)
    config = read_file(config_path)
    if config is None:
        raise RuntimeError(f"Failed to read YAML config from: {config_path}")

    # Build server address
    server_address = f"{args.server_host}:{args.port}"

    # Init environment
    env = PhototaxisEnv(server_address, args.client_name)
    env.connect_to_client()
    env.init(config)

    # Agent(s)
    agent = QAgent(env, episodes=args.episodes)
    agents = {FIXED_AGENT_ID: agent}

    # Compute checkpoint base & show effective settings
    checkpoint_base = infer_checkpoint_base(config_path, args.checkpoint_dir)
    print_effective_config(args, config_path, checkpoint_base)

    # Train
    run_episodes(
        env=env,
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
