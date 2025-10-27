import matplotlib.pyplot as plt


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
