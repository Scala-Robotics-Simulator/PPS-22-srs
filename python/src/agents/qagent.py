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
    def __init__(self,
        env,
        epsilon_max=1.0,
        epsilon_min=0.001,
        epsilon_decay=0.01,
        alpha=0.5,
        gamma=0.99,
    ):
        self.env = env
        self.epsilon_max = epsilon_max
        self.epsilon_min = epsilon_min
        self.epsilon_decay = epsilon_decay
        self.alpha = alpha
        self.gamma = gamma

        self.Q = np.zeros((self.env.observation_space.n, self.env.action_space.n))
        self.epsilon = self.epsilon_max

    def choose_action(self, state):
        """Selects an action based on the current state using the epsilon-greedy policy."""
        if np.random.rand() < self.epsilon:
            return self.env.action_space.sample()
        else:
            return np.argmax(self.Q[state])

    def update_q(self, state, action, reward, next_state, done):
        """Updates the Q-value for a given state-action pair using the Q-learning formula."""
        best_next = np.max(self.Q[next_state])
        target = reward + (0 if done else self.gamma * best_next)
        self.Q[state, action] = (1 - self.alpha) * self.Q[state, action] + self.alpha * target

    def decay_epsilon(self):
        self.epsilon = max(self.epsilon_min, self.epsilon - self.epsilon_decay)