import matplotlib.pyplot as plt

def plot_learning_history(learning_history, fig_size=(15, 5), show=True, save_path=None):
    """
    Plot the learning process for Q-Learning (or multi-agent) agents.

    Parameters
    ----------
    learning_history : list of lists of dict
        Each element is a list of steps for one episode, each step is a dict containing 'total_reward'.
    fig_size : tuple, optional
        Size of the figure (default=(15,5)).
    show : bool, optional
        Whether to display the plot immediately (default=True).
    save_path : str, optional
        If provided, saves the plot to this path.
    """
    steps = [len(episode) for episode in learning_history]
    total_rewards = [episode[-1]['total_reward'] for episode in learning_history]

    fig, ax1 = plt.subplots(figsize=fig_size)

    ax1.plot(range(len(steps)), steps, color='orange')
    ax1.tick_params(axis='y', labelcolor='orange')
    ax1.set_xlabel('Episodes')
    ax1.set_ylabel('Steps', color='orange')

    ax2 = ax1.twinx()
    ax2.plot(range(len(total_rewards)), total_rewards, color='#1f77b4')
    ax2.tick_params(axis='y', labelcolor='#1f77b4')
    ax2.set_ylabel('Total reward', color='#1f77b4')

    fig.tight_layout()

    if save_path:
        plt.savefig(save_path)

    if show:
        plt.show()
    else:
        plt.close(fig)
