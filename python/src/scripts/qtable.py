#!/usr/bin/env python3
"""
Modes:
  • Training (default)
  • Curriculum (--curriculum)
  • Plot (--plot <csv>)
"""

from __future__ import annotations
import argparse
import asyncio
import os
import sys
import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime
from pathlib import Path
from collections import deque
import numpy as np
from tqdm import tqdm

# -----------------------------------------------------------------------------
# Imports and async setup
# -----------------------------------------------------------------------------
sys.path.append("..")
import nest_asyncio
from utils.log import Logger
from utils.reader import get_yaml_path, read_file
from agent.qagent import QAgent
from environment.qlearning.phototaxis_env import PhototaxisEnv

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

nest_asyncio.apply()


logger = Logger(__name__)

# =============================================================================
# Global defaults
# =============================================================================
DEFAULTS = {
    "config_root": ("resources", "configurations"),
    "server_host": "localhost",
    "port": 50051,
    "episodes": 10000,
    "steps": 2000,
    "alpha": 0.3,
    "gamma": 0.99,
    "epsilon_min": 0.03,
    "epsilon_max": 1.0,
    "checkpoint_interval": 5000,
    "checkpoint_dir": None,
    "load_checkpoint": None,
    "client_name": "PhototaxisRLClient",
    "encoder": "light9_plus_prox3",
    "action": "gentle4",
}
FIXED_AGENT_ID = "00000000-0000-0000-0000-000000000001"


# =============================================================================
# Argument parsing and utilities
# =============================================================================
def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="Phototaxis Q-learning — adaptive trainer")

    # environment and config
    # To this (to match your new env):
    p.add_argument(
        "--encoder",
        type=str,
        default="light9",
        choices=[
            "light4",
            "light8",
            "light9",
            "compact36",
            "prox8_3bins",
            "light4_plus_flags",
            "light9_plus_prox3",
        ],
        help="State encoder variant (default: light9)",
    )
    p.add_argument(
        "--action",
        type=str,
        default="gentle4",
        choices=["gentle4", "hard3", "pivot6"],
        help="Action set to use (default: gentle4)",
    )
    p.add_argument(
        "--scenario",
        type=str,
        default="phototaxis",
        help="Reward scenario family (affects YAML filenames)",
    )
    p.add_argument(
        "--config",
        type=str,
        help="Optional YAML config name (overrides scenario autodetection)",
    )

    # hyperparameters
    p.add_argument("--server-host", type=str, default=DEFAULTS["server_host"])
    p.add_argument("--port", type=int, default=DEFAULTS["port"])
    p.add_argument("--episodes", type=int, default=DEFAULTS["episodes"])
    p.add_argument("--steps", type=int, default=DEFAULTS["steps"])
    p.add_argument("--alpha", type=float, default=DEFAULTS["alpha"])
    p.add_argument("--gamma", type=float, default=DEFAULTS["gamma"])
    p.add_argument("--epsilon-min", type=float, default=DEFAULTS["epsilon_min"])
    p.add_argument(
        "--checkpoint-interval", type=int, default=DEFAULTS["checkpoint_interval"]
    )
    p.add_argument("--checkpoint-dir", type=str, default=DEFAULTS["checkpoint_dir"])
    p.add_argument("--load-checkpoint", type=str, default=DEFAULTS["load_checkpoint"])
    p.add_argument("--client-name", type=str, default=DEFAULTS["client_name"])

    # adaptive stop conditions
    p.add_argument(
        "--stop-epsilon",
        type=float,
        default=0.1,
        help="Stop when epsilon ≤ this threshold",
    )
    p.add_argument(
        "--stop-coverage",
        type=float,
        default=0.98,
        help="Stop when state coverage ≥ this ratio (0–1)",
    )
    p.add_argument(
        "--stable-window",
        type=int,
        default=10,
        help="Require convergence to be stable for this many episodes",
    )

    # modes
    p.add_argument(
        "--curriculum", action="store_true", help="Run curriculum (easy→medium→hard)"
    )
    p.add_argument("--plot", type=str, help="Plot metrics from a CSV (no training)")
    return p.parse_args()


def resolve_config_path(name: str) -> str:
    if not name.endswith((".yml", ".yaml")):
        name += ".yml"
    base_a, base_b = DEFAULTS["config_root"]
    return get_yaml_path(base_a, base_b, name)


def infer_checkpoint_base(
    encoder: str, config_path: str, explicit_dir: str | None
) -> str:
    if explicit_dir:
        return explicit_dir.rstrip("/")
    stem = Path(config_path).stem
    return f"checkpoints/{encoder}_{stem}"


# =============================================================================
# Core training loop
# =============================================================================
def run_training(
    env: PhototaxisEnv,
    agent: QAgent,
    agent_id: str,
    max_episodes: int,
    steps: int,
    checkpoint_base: str,
    interval: int,
    load_ckpt: str | None = None,
    stop_epsilon: float = 0.1,
    stop_coverage: float = 0.98,
    stable_window: int = 10,
    csv_log: str = "logs/training.csv",
) -> str:
    os.makedirs(Path(csv_log).parent, exist_ok=True)
    os.makedirs(Path(checkpoint_base).parent, exist_ok=True)

    if load_ckpt:
        agent.load(load_ckpt)
        agent.epsilon = agent.epsilon_min
        logger.info(f"Warm start: {load_ckpt}")

    csv_header = not Path(csv_log).exists()
    best_reward = -float("inf")
    total_terminated = 0
    reward_window, termination_window = deque(maxlen=50), deque(maxlen=50)
    best_path = None
    stable_counter = 0
    ep_idx = 0

    with open(csv_log, "a", encoding="utf-8") as f_log:
        if csv_header:
            f_log.write("episode,total_reward,terminated,epsilon,coverage\n")

        pbar = tqdm(
            total=max_episodes if max_episodes > 0 else None, desc="Training", unit="ep"
        )

        while True:
            obs, _ = env.reset()
            total_reward = 0.0
            terminated = False

            for step in range(steps):
                actions = {agent_id: agent.choose_action(obs[agent_id])}
                next_obs, rewards, term, trunc, _ = env.step(actions)
                done = term[agent_id] or trunc[agent_id]
                agent.update_q(
                    obs[agent_id],
                    actions[agent_id],
                    rewards[agent_id],
                    next_obs[agent_id],
                    term[agent_id],
                    trunc[agent_id],
                )
                total_reward += rewards[agent_id]
                obs = next_obs
                if done:
                    terminated = term[agent_id]
                    msg = "TERMINATED" if term[agent_id] else "TRUNCATED"
                    logger.info(
                        f"[Ep {ep_idx + 1}] {msg} step={step} R={total_reward:.2f}"
                    )
                    if term[agent_id]:
                        total_terminated += 1
                        termination_window.append(1)
                        logger.info(f"Episode {ep_idx + 1} terminated successfully.")
                        # agent.save(f"{checkpoint_base}_terminated_ep{ep_idx+1}")
                    else:
                        termination_window.append(0)
                    break

            visited = np.count_nonzero(np.any(agent.Q != 0, axis=1))
            coverage = visited / agent.Q.shape[0]
            reward_window.append(total_reward)
            mean_r = np.mean(reward_window)
            term_rate = 100 * np.mean(termination_window) if termination_window else 0

            f_log.write(
                f"{ep_idx + 1},{total_reward},{int(terminated)},{agent.epsilon:.4f},{coverage:.4f}\n"
            )
            f_log.flush()

            # autosaves
            if total_reward > best_reward:
                best_reward = total_reward
                best_path = f"{checkpoint_base}_top_ep{ep_idx + 1}"
                agent.save(best_path)
                logger.info(f"New best reward={best_reward:.2f}")

            if interval > 0 and (ep_idx + 1) % interval == 0:
                agent.save(f"{checkpoint_base}_ep{ep_idx + 1}")
                q_stats(agent, ep_idx + 1)
                logger.info(
                    f"📈 AvgR={mean_r:.2f} | TermRate={term_rate:.1f}% | ε={agent.epsilon:.3f} | Coverage={coverage * 100:.1f}%"
                )

            # epsilon decay
            agent.decay_epsilon(ep_idx)

            # --- stable early stop ---
            if agent.epsilon <= stop_epsilon and coverage >= stop_coverage:
                stable_counter += 1
                if stable_counter >= stable_window:
                    logger.info(
                        f"Early stopping stable for {stable_window} episodes "
                        f"(ε={agent.epsilon:.3f}, coverage={coverage * 100:.1f}%)"
                    )
                    break
            else:
                stable_counter = 0

            # --- max episodes stop ---
            ep_idx += 1
            pbar.update(1)
            if max_episodes > 0 and ep_idx >= max_episodes:
                logger.info(f"Reached max episodes limit ({max_episodes})")
                break

        pbar.close()

    agent.save(f"{checkpoint_base}_final")
    logger.info(
        f"Training complete | Terminated={total_terminated} | BestR={best_reward:.2f}"
    )
    return best_path or f"{checkpoint_base}_final"


# =============================================================================
# Curriculum manager
# =============================================================================
def run_curriculum(args: argparse.Namespace) -> None:
    curriculum = [
        (f"phototaxis_{args.scenario}_easy.yml", 20000, 20000),
        (f"phototaxis_{args.scenario}_medium.yml", 30000, 25000),
        (f"phototaxis_{args.scenario}_hard.yml", 40000, 30000),
    ]
    last_ckpt = None
    for name, episodes, steps in curriculum:
        config_path = resolve_config_path(name)
        config = read_file(config_path)
        if not config:
            raise FileNotFoundError(f"Missing YAML: {config_path}")

        checkpoint_base = infer_checkpoint_base(
            args.encoder, config_path, args.checkpoint_dir
        )
        csv_log = f"logs/curriculum_{Path(config_path).stem}.csv"

        logger.info(f"\nTraining phase: {name} | episodes={episodes}, steps={steps}")

        env = PhototaxisEnv(
            f"{args.server_host}:{args.port}",
            args.client_name,
            encoder_name=args.encoder,
        )
        env.connect_to_client()
        env.init(config)

        agent = QAgent(
            env,
            episodes=episodes,
            epsilon_max=DEFAULTS["epsilon_max"],
            epsilon_min=args.epsilon_min,
            alpha=args.alpha,
            gamma=args.gamma,
        )

        last_ckpt = run_training(
            env,
            agent,
            FIXED_AGENT_ID,
            episodes,
            steps,
            checkpoint_base,
            args.checkpoint_interval,
            load_ckpt=last_ckpt,
            stop_epsilon=args.stop_epsilon,
            stop_coverage=args.stop_coverage,
            stable_window=args.stable_window,
            csv_log=csv_log,
        )
        env.close()

    logger.info(
        f"Curriculum complete for scenario '{args.scenario}'. Final checkpoint: {last_ckpt}"
    )


# =============================================================================
# Plotting
# =============================================================================
def plot_metrics(csv_path: str) -> None:
    if not os.path.exists(csv_path):
        logger.error(f"Missing CSV: {csv_path}")
        return

    df = pd.read_csv(csv_path)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M")
    outdir = Path("logs/plots")
    outdir.mkdir(parents=True, exist_ok=True)
    outpath = outdir / f"{Path(csv_path).stem}_{timestamp}.png"

    fig, ax = plt.subplots(4, 1, figsize=(10, 12), sharex=True)  # 4 plots, 12 height
    fig.suptitle(f"{Path(csv_path).stem}", fontsize=14, weight="bold")

    if "coverage" in df.columns:
        ax[3].plot(
            df["episode"],
            df["coverage"] * 100,
            color="purple",
            label="State Coverage (%)",
        )
        ax[3].legend()
        ax[3].set_ylabel("Coverage %")
        ax[3].set_ylim(0, 100)

    if "total_reward" in df.columns:
        df["reward_smooth"] = (
            df["total_reward"].rolling(window=50, min_periods=1).mean()
        )
        ax[0].plot(
            df["episode"],
            df["reward_smooth"],
            color="royalblue",
            label="Reward (smooth)",
        )
        ax[0].legend()
        ax[0].set_ylabel("Reward")

    if "terminated" in df.columns:
        term_rate = df["terminated"].rolling(window=100, min_periods=1).mean() * 100
        ax[1].plot(
            df["episode"], term_rate, color="darkorange", label="Termination (%)"
        )
        ax[1].legend()
        ax[1].set_ylabel("Termination %")

    if "epsilon" in df.columns:
        ax[2].plot(df["episode"], df["epsilon"], color="green", label="Epsilon decay")
        ax[2].legend()
        ax[2].set_ylabel("Epsilon")

    for a in ax:
        a.grid(True, alpha=0.3)
    ax[-1].set_xlabel("Episode")
    plt.tight_layout(rect=[0, 0, 1, 0.96])
    plt.savefig(outpath, dpi=200)
    logger.info(f"Saved plot → {outpath}")


# =============================================================================
# Diagnostics
# =============================================================================
def q_stats(agent: QAgent, episode: int) -> None:
    q = agent.Q
    visited = np.count_nonzero(np.any(q != 0, axis=1))
    total = q.shape[0]
    logger.info("=" * 70)
    logger.info(
        f"[Checkpoint @ ep{episode}] Visited={visited}/{total} ({100 * visited / total:.2f}%)"
    )
    logger.info(f"  Q-range: {q.min():.2f} → {q.max():.2f} | mean={q.mean():.2f}")
    logger.info("=" * 70)


# =============================================================================
# Entry point
# =============================================================================
def main() -> None:
    args = parse_args()

    if args.plot:
        plot_metrics(args.plot)
        return

    if args.curriculum:
        run_curriculum(args)
        return

    config_name = args.config or f"phototaxis_{args.scenario}_easy.yml"
    config_path = resolve_config_path(config_name)
    config = read_file(config_path)
    if not config:
        raise FileNotFoundError(config_path)

    checkpoint_base = infer_checkpoint_base(
        args.encoder, config_path, args.checkpoint_dir
    )
    env = PhototaxisEnv(
        f"{args.server_host}:{args.port}",
        args.client_name,
        encoder_name=args.encoder,
        action_set=args.action,
    )
    env.connect_to_client()
    env.init(config)

    agent = QAgent(
        env,
        episodes=args.episodes,
        epsilon_max=DEFAULTS["epsilon_max"],
        epsilon_min=args.epsilon_min,
        alpha=args.alpha,
        gamma=args.gamma,
    )

    run_training(
        env,
        agent,
        FIXED_AGENT_ID,
        args.episodes,
        args.steps,
        checkpoint_base,
        args.checkpoint_interval,
        args.load_checkpoint,
        args.stop_epsilon,
        args.stop_coverage,
        args.stable_window,
    )

    env.close()
    logger.info("Connection closed successfully.")


if __name__ == "__main__":
    main()
