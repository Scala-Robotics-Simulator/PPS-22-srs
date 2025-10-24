import math

import numpy as np


class QAgent:
    """
    Q-learning agent with epsilon-greedy exploration strategy.

    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    epsilon_max : float, optional (default=1.0)
        Initial exploration rate (epsilon).
    epsilon_min : float, optional (default=0.001)
        Minimum exploration rate.
    epsilon_decay : float, optional (default=0.01)
        Decay rate for epsilon after each episode.
    alpha : float, optional (default=0.5)
        Learning rate.
    gamma : float, optional (default=0.99)
        Discount factor for future rewards.

    Attributes
    ----------
    Q : np.ndarray
        Q-table mapping state-action pairs to their estimated values.
    epsilon : float
        Current exploration rate.
    """

    def __init__(
        self,
        env,
        epsilon_max: float = 1.0,
        epsilon_min: float = 0.001,
        alpha: float = 0.5,
        gamma: float = 0.99,
        episodes: int = 1000,
    ):
        self.episodes = episodes
        self.env = env
        self.epsilon_max = epsilon_max
        self.epsilon_min = epsilon_min
        self.epsilon_decay = -math.log(self.epsilon_min) / self.episodes
        self.alpha = alpha
        self.gamma = gamma

        self.Q = np.zeros((self.env.observation_space_n, self.env.action_space.n))
        self.epsilon = self.epsilon_max

    def choose_action(self, state: int, epsilon_greedy: bool = True):
        """Selects an action based on the current state using the epsilon-greedy policy.

        Parameters
        ----------
        state : int
            The current state of the environment.
        Returns
        -------
        action : int
            The action chosen by the agent.
        """
        if np.random.rand() < self.epsilon and epsilon_greedy:
            return self.env.action_space.sample()
        return np.argmax(self.Q[state])

    def update_q(
        self, state: int, action: int, reward: float, next_state: int, done: bool
    ):
        """Updates the Q-value for a given state-action pair using the Q-learning formula.

        Parameters
        ----------
        state : int
            The current state of the environment.
        action : int
            The action taken by the agent.
        reward : float
            The reward received after taking the action.
        next_state : int
            The state of the environment after taking the action.
        done : bool
            Whether the episode has ended.
        """
        best_next = np.max(self.Q[next_state])
        target = reward + (0 if done else self.gamma * best_next)
        self.Q[state, action] = (1 - self.alpha) * self.Q[
            state, action
        ] + self.alpha * target

    def decay_epsilon(self, episode: int):
        """Decays the exploration rate epsilon after each episode."""
        self.epsilon = self.epsilon_min + (
            self.epsilon_max - self.epsilon_min
        ) * math.exp(-self.epsilon_decay * episode)
