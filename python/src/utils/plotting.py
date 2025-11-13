import matplotlib.pyplot as plt
import numpy as np


def plot_learning_history(
    learning_history, fig_size=(15, 5), show=True, save_path=None
):
    """
    Plot the learning process for multi-agent Q-Learning.

    Parameters
    ----------
    learning_history : dict[str, list of list of dict]
        Dict mapping agent_id to their episode histories.
    fig_size : tuple, optional
        Size of the figure (default=(15,5)).
    show : bool, optional
        Whether to display the plot immediately (default=True).
    save_path : str, optional
        If provided, saves the plot to this path.
    """
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


def plot_td_losses(td_losses: list[list[float]], episode_idx: list[int]) -> None:
    episode_means = [np.mean(ep) for ep in td_losses]

    alpha = 0.1
    smoothed = []
    for m in episode_means:
        if not smoothed:
            smoothed.append(m)
        else:
            smoothed.append(alpha * m + (1 - alpha) * smoothed[-1])

    plt.figure(figsize=(8, 4))
    plt.plot(episode_idx, episode_means, label="Mean TD Loss per Episode", linewidth=1)
    plt.plot(episode_idx, smoothed, label=f"Smoothed (α={alpha})", linewidth=2)
    plt.xlabel("Episode Index")
    plt.ylabel("TD Loss")
    plt.title("Smoothed Temporal Difference Loss over Evaluation Episodes")
    plt.legend()
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.tight_layout()
    plt.show()
