import matplotlib.pyplot as plt
import numpy as np


def plot_learning_history(
    learning_history, fig_size=(15, 5), show=True, save_path=None
):
    fig, ax1 = plt.subplots(figsize=fig_size)
    ax2 = ax1.twinx()

    for agent_id, agent_history in learning_history.items():
        steps = [ep["steps"] for ep in agent_history]
        total_rewards = [ep["total_reward"] for ep in agent_history]

        ax1.plot(
            range(len(steps)),
            steps,
            label=f"{agent_id} steps",
            linestyle="--",
            color="orange",
        )
        ax2.plot(
            range(len(total_rewards)),
            total_rewards,
            label=f"{agent_id} reward",
            color="#1f77b4",
        )

    ax1.set_xlabel("Episodes")
    ax1.set_ylabel("Steps", color="orange")
    ax2.set_ylabel("Total reward", color="#1f77b4")

    lines, labels = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines + lines2, labels + labels2, loc="upper left")

    fig.tight_layout()

    if save_path:
        plt.savefig(save_path)
    if show:
        plt.show()
    else:
        plt.close(fig)


def _smooth(data: list[float], alpha: float = 0.1) -> list[float]:
    if not data:
        return []
    smoothed = [data[0]]
    for x in data[1:]:
        smoothed.append(alpha * x + (1 - alpha) * smoothed[-1])
    return smoothed


def plot_total_reward(results: dict, agents: list[str], save_path: str = None):
    """
    Plot total reward per episode for each agent.
    Displays 2 agents side by side.
    """
    n_agents = len(agents)
    n_cols = 2
    n_rows = (n_agents + n_cols - 1) // n_cols

    fig, axes = plt.subplots(
        n_rows, n_cols, figsize=(10 * n_cols, 4 * n_rows), sharex=False
    )

    if isinstance(axes, np.ndarray):
        axes = axes.flatten()
    else:
        axes = [axes]

    for idx, agent_id in enumerate(agents):
        ax = axes[idx]
        total_rewards = results["total_rewards"][agent_id]
        if not total_rewards:
            continue

        episodes = range(1, len(total_rewards) + 1)
        ax.plot(
            episodes,
            total_rewards,
            marker="o",
            markersize=3,
            alpha=0.7,
            label="Total Reward",
        )

        if len(total_rewards) > 1:
            z = np.polyfit(episodes, total_rewards, 1)
            p = np.poly1d(z)
            ax.plot(episodes, p(episodes), "r--", alpha=0.8, linewidth=2, label="Trend")

        success_rate = results["success_rate"][agent_id]
        ax.text(
            0.02,
            0.98,
            f"Success Rate: {success_rate:.2%}",
            transform=ax.transAxes,
            verticalalignment="top",
            bbox={"boxstyle": "round", "facecolor": "lightgreen", "alpha": 0.5},
        )

        ax.set_title(f"{agent_id} – Total Reward per Episode")
        ax.set_xlabel("Episode")
        ax.set_ylabel("Total Reward")
        ax.legend()
        ax.grid(True, alpha=0.3)

    # Hide any unused subplots
    for idx in range(n_agents, len(axes)):
        axes[idx].set_visible(False)

    plt.tight_layout()
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
    plt.show()


def plot_td_loss_success(results: dict, agents: list[str], save_path: str = None):
    """
    Plot TD loss for successful episodes only for each agent.
    """
    n_agents = len(agents)
    _, axes = plt.subplots(n_agents, 1, figsize=(10, 4 * n_agents), sharex=False)

    if n_agents == 1:
        axes = [axes]

    for idx, agent_id in enumerate(agents):
        ax = axes[idx]
        td_losses = results["td_losses"][agent_id]
        successes_idx = results["successes_idx"][agent_id]

        if not td_losses or not successes_idx:
            continue

        successful_td_losses = [
            td_losses[i] for i in successes_idx if i < len(td_losses)
        ]
        episode_means = [np.mean(ep) for ep in successful_td_losses if len(ep) > 0]

        if episode_means:
            smoothed = _smooth(episode_means)
            ax.plot(
                successes_idx[: len(episode_means)],
                episode_means,
                label="Mean TD Loss",
                alpha=0.6,
            )
            ax.plot(
                successes_idx[: len(smoothed)], smoothed, label="Smoothed", linewidth=2
            )
            ax.set_title(f"{agent_id} – TD Loss (Successful Episodes)")
            ax.set_xlabel("Episode Index")
            ax.set_ylabel("TD Loss")
            ax.legend()
            ax.grid(True, linestyle="--", alpha=0.5)

    plt.tight_layout()
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
    plt.show()


def plot_td_loss_all(results: dict, agents: list[str], save_path: str = None):
    """
    Plot TD loss for all episodes for each agent.
    """
    n_agents = len(agents)
    _, axes = plt.subplots(n_agents, 1, figsize=(10, 4 * n_agents), sharex=False)

    if n_agents == 1:
        axes = [axes]

    for idx, agent_id in enumerate(agents):
        ax = axes[idx]
        td_losses = results["td_losses"][agent_id]
        if not td_losses:
            continue

        episode_means = [np.mean(ep) for ep in td_losses if len(ep) > 0]
        episode_indices = range(len(episode_means))
        smoothed = _smooth(episode_means)

        ax.plot(episode_indices, episode_means, label="Mean TD Loss", alpha=0.6)
        ax.plot(episode_indices, smoothed, label="Smoothed", linewidth=2)
        ax.set_title(f"{agent_id} – TD Loss (All Episodes)")
        ax.set_xlabel("Episode Index")
        ax.set_ylabel("TD Loss")
        ax.legend()
        ax.grid(True, linestyle="--", alpha=0.5)

    plt.tight_layout()
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
    plt.show()


def plot_avg_reward_per_configuration(
    results: dict, agents: list[str], save_path: str = None
):
    """
    Compare moving average reward per episode across agents for each configuration.
    Plots up to 3 configurations side by side.
    """
    n_configs = max(len(results["moving_avg_reward"][a]) for a in agents)
    n_cols = min(3, n_configs)
    n_rows = (n_configs + n_cols - 1) // n_cols

    _, axes = plt.subplots(
        n_rows, n_cols, figsize=(5 * n_cols, 4 * n_rows), sharex=False
    )

    # Flatten axes array for easier indexing
    if isinstance(axes, np.ndarray):
        axes = axes.flatten()
    else:
        axes = [axes]

    for cfg_idx in range(n_configs):
        ax = axes[cfg_idx]
        for agent_id in agents:
            moving_avg = results["moving_avg_reward"][agent_id]
            if cfg_idx < len(moving_avg) and isinstance(moving_avg[cfg_idx], list):
                episode_data = moving_avg[cfg_idx]
                if len(episode_data) > 0:
                    steps = range(len(episode_data))
                    ax.plot(
                        steps, episode_data, label=agent_id, linewidth=1.5, alpha=0.7
                    )
        ax.set_title(
            f"Configuration {cfg_idx + 1} – Moving Average Reward (All Agents)"
        )
        ax.set_xlabel("Steps")
        ax.set_ylabel("Moving Average Reward")
        ax.legend()
        ax.grid(True, alpha=0.3)

    # Hide any unused subplots
    for idx in range(n_configs, len(axes)):
        axes[idx].set_visible(False)

    plt.tight_layout()
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
    plt.show()


def plot_steps_until_success_box(
    results: dict, agents: list[str], save_path: str = None
):
    """
    Create a box plot of steps-to-success for each agent.

    Parameters
    ----------
    results : Dict
        Expected keys:
          - "steps_to_success": Dict[agent_id, List[int]] (or list-like)
        Other keys are ignored.
    agents : List[str]
        Order and subset of agents to plot.
    save_path : Optional[str]
        If provided, save the figure to this path.
    """
    # Collect data per agent
    box_data = []
    labels = []
    positions = []

    for idx, agent_id in enumerate(agents):
        steps = results.get("steps_to_success", {}).get(agent_id)
        if not steps:
            continue
        # Ensure numeric list
        cleaned = [s for s in steps if s is not None]
        if cleaned:
            box_data.append(cleaned)
            labels.append(agent_id)
            positions.append(idx + 1)

    if not box_data:
        return

    _, ax = plt.subplots(figsize=(max(6, len(box_data) * 1.5), 6))
    bp = ax.boxplot(
        box_data,
        labels=labels,
        positions=positions,
        patch_artist=True,
        widths=0.6,
    )

    # Color each box differently using a colormap
    colors = plt.cm.Set3(np.linspace(0, 1, len(box_data)))
    for patch, color in zip(bp["boxes"], colors, strict=False):
        patch.set_facecolor(color)
        patch.set_alpha(0.6)

    # Style the median lines
    for median in bp["medians"]:
        median.set_color("red")
        median.set_linewidth(2)

    ax.set_title("Steps Until Success (per agent)")
    ax.set_ylabel("Steps")
    ax.set_xlabel("Agent")
    ax.grid(True, axis="y", alpha=0.3)

    plt.tight_layout()
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
    plt.show()


def plot_all_q_agent(results: dict, agents: list[str]):
    """
    Run all plot types in sequence.
    """
    plot_total_reward(results, agents)
    # plot_td_loss_success(results, agents)
    # plot_td_loss_all(results, agents)
    plot_avg_reward_per_configuration(results, agents)
    plot_steps_until_success_box(results, agents)


def plot_all(results: dict, agents: list[str]):
    """
    Run all plot types in sequence.
    """
    plot_total_reward(results, agents)
    plot_td_loss_success(results, agents)
    plot_td_loss_all(results, agents)
    plot_avg_reward_per_configuration(results, agents)
    plot_steps_until_success_box(results, agents)
