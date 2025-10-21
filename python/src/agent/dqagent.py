import random
from collections import deque

import numpy as np


class DQAgent:
    """
    Deep Q-learning agent with epsilon-greedy exploration strategy and experience replay.
    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    epsilon_max : float, optional (default=1.0)
        Initial exploration rate (epsilon).
    epsilon_min : float, optional (default=0.01)
        Minimum exploration rate.
    epsilon_decay : float, optional (default=0.0002)
        Decay rate for epsilon after each step.
    gamma : float, optional (default=0.99)
        Discount factor for future rewards.
    replay_memory_max_size : int, optional (default=100000)
        Maximum size of the replay memory.
    replay_memory_init_size : int, optional (default=1000)
        Initial size of the replay memory.
    batch_size : int, optional (default=64)
        Size of mini-batches sampled from replay memory.
    step_per_update : int, optional (default=4)
        Number of steps between each model update.
    step_per_update_target_model : int, optional (default=8)
        Number of steps between each target model update.
    moving_avg_window_size : int, optional (default=20)
        Window size for moving average calculation.
    moving_avg_stop_thr : float, optional (default=100)
        Threshold for stopping criteria based on moving average.
    episode_max_steps : int, optional (default=400)
        Maximum number of steps per episode during replay memory initialization.

    Attributes
    ----------
    replay_memory : deque
        Experience replay memory storing past transitions.
    epsilon : float
        Current exploration rate.
    """

    def __init__(
        self,
        env,
        epsilon_max: float = 1.0,
        epsilon_min: float = 0.01,
        epsilon_decay: float = 0.0002,
        gamma: float = 0.99,
        replay_memory_max_size: int = 100000,
        replay_memory_init_size: int = 1000,
        batch_size: int = 64,
        step_per_update: int = 4,
        step_per_update_target_model: int = 8,
        moving_avg_window_size: int = 20,
        moving_avg_stop_thr: int = 100,
        episode_max_steps: int = 400,
    ):
        self.env = env
        self.epsilon_max = epsilon_max
        self.epsilon_min = epsilon_min
        self.epsilon_decay = epsilon_decay
        self.epsilon = epsilon_max
        self.gamma = gamma
        self.batch_size = batch_size
        self.step_per_update = step_per_update
        self.step_per_update_target_model = step_per_update_target_model
        self.moving_avg_window_size = moving_avg_window_size
        self.moving_avg_stop_thr = moving_avg_stop_thr

        self.replay_memory = deque(maxlen=replay_memory_max_size)

        if replay_memory_init_size > 0:
            self.simple_dqn_replay_memory_init(
                env, self.replay_memory, replay_memory_init_size, episode_max_steps
            )

    def choose_action(self, state: np.ndarray, dqn_action_model):
        """Select an action using epsilon-greedy policy.

        Parameters
        ----------
        state : np.ndarray
            Current state of the environment.
        dqn_action_model : keras.Sequential
            The action model used to predict Q-values.

        Returns
        -------
        action : int
            Selected action.
        """
        if random.uniform(0, 1) <= self.epsilon:
            return self.env.action_space.sample()
        q_values = dqn_action_model.predict(state[np.newaxis], verbose=0)
        return np.argmax(q_values)

    def store_transition(
        self,
        state: np.ndarray,
        action: int,
        reward: float,
        next_state: np.ndarray,
        done: bool,
    ):
        """Store transition in replay memory.

        Parameters
        ----------
        state : np.ndarray
            Current state.
        action : int
            Action taken.
        reward : float
            Reward received.
        next_state : np.ndarray
            Next state after taking the action.
        done : bool
            Whether the episode has ended.
        """
        self.replay_memory.append([state, action, reward, next_state, done])

    def get_random_batch(self):
        """Retrieve a random mini-batch from replay memory.

        Returns
        -------
        batch : tuple of np.ndarray
            Mini-batch containing states, actions, rewards, next_states, and done flags.
        """
        return self.get_random_batch_from_replay_memory(
            self.replay_memory, self.batch_size
        )

    def update_target_model(self, dqn_action_model, dqn_target_model):
        """Copy weights from action to a target model.
        Parameters
        ----------
        dqn_action_model : keras.Sequential
            The action model.
        dqn_target_model : keras.Sequential
            The target model.
        """
        dqn_target_model.set_weights(dqn_action_model.get_weights())

    def decay_epsilon(self):
        """Decay the exploration rate epsilon."""
        self.epsilon = max(self.epsilon_min, self.epsilon - self.epsilon_decay)

    def simple_dqn_replay_memory_init(
        self,
        env,
        replay_memory: deque,
        replay_memory_init_size: int,
        episode_max_steps: int,
    ):
        """Initialize replay memory with random transitions.
        Parameters
        ----------
        env : gym.Env
            The environment in which the agent interacts.
        replay_memory : deque
            The replay memory to be initialized.
        replay_memory_init_size : int
            The desired initial size of the replay memory.
        episode_max_steps : int
            Maximum number of steps per episode during initialization.
        """
        while len(replay_memory) < replay_memory_init_size:
            state, info = env.reset()
            done = False
            step_count = 0

            while (step_count < episode_max_steps) and (not done):
                action = env.action_space.sample()
                new_state, reward, terminated, truncated, info = env.step(action)
                done = terminated or truncated

                replay_memory.append([state, action, reward, new_state, done])

                state = new_state
                step_count += 1

    def get_random_batch_from_replay_memory(
        self, replay_memory: deque, batch_size: int
    ):
        """Retrieve a random mini-batch from the given replay memory.
        Parameters
        ----------
        replay_memory : deque
            The replay memory from which to sample.
        batch_size : int
            The size of the mini-batch to sample.
        Returns
        -------
        batch : tuple of np.ndarray
            Mini-batch containing states, actions, rewards, next_states, and done flags.
        """
        minibatch_indices = np.random.choice(range(len(replay_memory)), size=batch_size)
        minibatch = [replay_memory[i] for i in minibatch_indices]

        state_batch = np.array([sample[0] for sample in minibatch])
        action_batch = np.array([sample[1] for sample in minibatch])
        reward_batch = np.array([sample[2] for sample in minibatch])
        new_state_batch = np.array([sample[3] for sample in minibatch])
        done_batch = np.array([sample[4] for sample in minibatch])

        return [state_batch, action_batch, reward_batch, new_state_batch, done_batch]
