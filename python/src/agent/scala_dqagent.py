import math
import os
import random
from collections import deque

import numpy as np
import tensorflow as tf

from training.dqnetwork import DQNetwork


class DQAgent:
    """
    Deep Q-learning agent with epsilon-greedy exploration strategy and experience replay.
    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    agent_id : str
        The id of the agent
    action_model : DQNetwork
        The main Q-network, trained at each step.
    target_model : DQNetwork
        The target Q-network, periodically updated from the main model.
    epsilon_max : float, optional (default=1.0)
        Initial exploration rate (epsilon).
    epsilon_min : float, optional (default=0.01)
        Minimum exploration rate.
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
    episodes : int, optional (default=1000)
        Number of training episodes.
    n_step : int, optional (default=5)
        Number of steps for n-step returns.

    Attributes
    ----------
    replay_memory : deque
        Experience replay memory storing past transitions.
    epsilon : float
        Current exploration rate.
    terminated : bool
        Flag indicating whether the agent has terminated.
    """

    def __init__(
        self,
        env,
        agent_id,
        action_model: DQNetwork,
        target_model: DQNetwork,
        epsilon_max: float = 1.0,
        epsilon_min: float = 0.01,
        gamma: float = 0.99,
        replay_memory_max_size: int = 100000,
        replay_memory_init_size: int = 1000,
        batch_size: int = 64,
        step_per_update: int = 4,
        step_per_update_target_model: int = 8,
        moving_avg_window_size: int = 20,
        moving_avg_stop_thr: int = 100,
        episode_max_steps: int = 400,
        episodes: int = 1000,
        n_step: int = 1,  # NEW: number of steps for n-step returns
    ):
        self.env = env
        self.id = agent_id
        self.action_model = action_model.model
        self.target_model = target_model.model
        self.target_model.set_weights(self.action_model.get_weights())
        self.epsilon_max = epsilon_max
        self.epsilon_min = epsilon_min
        self.epsilon_decay = -math.log(self.epsilon_min) / episodes
        self.epsilon = epsilon_max
        self.gamma = gamma
        self.batch_size = batch_size
        self.step_per_update = step_per_update
        self.step_per_update_target_model = step_per_update_target_model
        self.moving_avg_window_size = moving_avg_window_size
        self.moving_avg_stop_thr = moving_avg_stop_thr
        self.episodes = episodes
        self.terminated = False
        self.n_step = n_step  # NEW

        self.replay_memory = deque(maxlen=replay_memory_max_size)
        self.n_step_buffer = deque(maxlen=n_step)  # NEW: buffer for n-step transitions

        # Create compiled TensorFlow functions for faster inference
        self._create_tf_functions()

        if replay_memory_init_size > 0:
            self.simple_dqn_replay_memory_init(
                env, self.replay_memory, replay_memory_init_size, episode_max_steps
            )

    def _create_tf_functions(self):
        """Create compiled TensorFlow functions for faster execution."""

        @tf.function
        def predict_q_values(model, state):
            """Compiled function for Q-value prediction."""
            return model(state, training=False)

        @tf.function
        def train_step(
            action_model,
            target_model,
            states,
            actions,
            rewards,
            next_states,
            dones,
            gamma,
            n_steps,
        ):
            """Compiled function for training step with n-step returns."""
            # Get target Q-values for next states
            next_q_values = target_model(next_states, training=False)
            max_next_q = tf.reduce_max(next_q_values, axis=1)

            # Calculate n-step target values
            # rewards already contain the n-step discounted sum
            # n_steps contains the actual discount factor to apply
            target_q = rewards + (1.0 - dones) * n_steps * max_next_q

            # Create masks for actions taken
            masks = tf.one_hot(actions, action_model.output_shape[-1])

            with tf.GradientTape() as tape:
                # Get current Q-values
                q_values = action_model(states, training=True)
                # Get Q-values for actions taken
                q_action = tf.reduce_sum(q_values * masks, axis=1)
                # Calculate loss
                loss = tf.reduce_mean(tf.square(target_q - q_action))

            # Apply gradients
            gradients = tape.gradient(loss, action_model.trainable_variables)
            action_model.optimizer.apply_gradients(
                zip(gradients, action_model.trainable_variables, strict=False)
            )

            return loss

        self._predict_q_values = predict_q_values
        self._train_step = train_step

    def choose_action(self, state: np.ndarray, epsilon_greedy: bool = True):
        """Select an action using epsilon-greedy policy."""
        if epsilon_greedy and random.uniform(0, 1) <= self.epsilon:
            return self.env.action_space.sample()

        # Use compiled TensorFlow function
        state_tensor = tf.constant(state[np.newaxis], dtype=tf.float32)
        q_values = self._predict_q_values(self.action_model, state_tensor)
        return int(tf.argmax(q_values[0]).numpy())

    def store_transition(
        self,
        state: np.ndarray,
        action: int,
        reward: float,
        next_state: np.ndarray,
        done: bool,
    ):
        """Store transition with n-step returns."""
        # Add to n-step buffer
        self.n_step_buffer.append([state, action, reward, next_state, done])

        # If buffer is full or episode ended, compute n-step return
        if len(self.n_step_buffer) == self.n_step or done:
            # Compute n-step return
            n_step_return = 0
            n_step_gamma = 1
            actual_n = len(self.n_step_buffer)

            for i in range(actual_n):
                n_step_return += n_step_gamma * self.n_step_buffer[i][2]  # reward
                n_step_gamma *= self.gamma

            # Get the first state and action, and the last next_state
            first_state = self.n_step_buffer[0][0]
            first_action = self.n_step_buffer[0][1]
            last_next_state = self.n_step_buffer[-1][3]
            last_done = self.n_step_buffer[-1][4]

            # Store the n-step transition
            # Format: [state, action, n_step_return, next_state_n, done, gamma^n]
            self.replay_memory.append(
                [
                    first_state,
                    first_action,
                    n_step_return,
                    last_next_state,
                    last_done,
                    self.gamma**actual_n,
                ]
            )

            # Clear buffer if episode ended
            if done:
                self.n_step_buffer.clear()

    def get_random_batch(self):
        """Retrieve a random mini-batch from replay memory."""
        return self.get_random_batch_from_replay_memory(
            self.replay_memory, self.batch_size
        )

    def update_target_model(self):
        """Copy weights from action to a target model."""
        self.target_model.set_weights(self.action_model.get_weights())

    def decay_epsilon(self, episode: int):
        """Decay the exploration rate epsilon."""
        self.epsilon = self.epsilon_min + (
            self.epsilon_max - self.epsilon_min
        ) * math.exp(-self.epsilon_decay * episode)

    def simple_dqn_replay_memory_init(
        self,
        env,
        replay_memory: deque,
        replay_memory_init_size: int,
        episode_max_steps: int,
    ):
        """Initialize replay memory with random transitions."""
        while len(replay_memory) < replay_memory_init_size:
            states, _ = env.reset()
            state = states[self.id]
            done = False
            step_count = 0

            while (step_count < episode_max_steps) and (not done):
                action = env.action_space.sample()
                actions = {self.id: action}
                new_states, rewards, terminateds, truncateds, _ = env.step(actions)
                done = terminateds[self.id] or truncateds[self.id]
                new_state = new_states[self.id]
                reward = rewards[self.id]

                self.store_transition(state, action, reward, new_state, done)

                state = new_state
                step_count += 1

    def get_random_batch_from_replay_memory(
        self, replay_memory: deque, batch_size: int
    ):
        """Retrieve a random mini-batch from the given replay memory."""
        minibatch_indices = np.random.choice(range(len(replay_memory)), size=batch_size)
        minibatch = [replay_memory[i] for i in minibatch_indices]

        state_batch = np.array([sample[0] for sample in minibatch], dtype=np.float32)
        action_batch = np.array([sample[1] for sample in minibatch], dtype=np.int32)
        reward_batch = np.array([sample[2] for sample in minibatch], dtype=np.float32)
        new_state_batch = np.array(
            [sample[3] for sample in minibatch], dtype=np.float32
        )
        done_batch = np.array([sample[4] for sample in minibatch], dtype=np.float32)
        gamma_n_batch = np.array([sample[5] for sample in minibatch], dtype=np.float32)

        return [
            state_batch,
            action_batch,
            reward_batch,
            new_state_batch,
            done_batch,
            gamma_n_batch,
        ]

    def dqn_update(self) -> float:
        """Perform a DQN update using the compiled TensorFlow function.

        Returns
        -------
        loss : float
            The TD loss value for this update.
        """
        (
            state_batch,
            action_batch,
            reward_batch,
            new_state_batch,
            done_batch,
            gamma_n_batch,
        ) = self.get_random_batch()

        # Convert to TensorFlow tensors
        states_tf = tf.constant(state_batch, dtype=tf.float32)
        actions_tf = tf.constant(action_batch, dtype=tf.int32)
        rewards_tf = tf.constant(reward_batch, dtype=tf.float32)
        next_states_tf = tf.constant(new_state_batch, dtype=tf.float32)
        dones_tf = tf.constant(done_batch, dtype=tf.float32)
        gamma_n_tf = tf.constant(gamma_n_batch, dtype=tf.float32)

        # Use compiled training function
        loss = self._train_step(
            self.action_model,
            self.target_model,
            states_tf,
            actions_tf,
            rewards_tf,
            next_states_tf,
            dones_tf,
            self.gamma,
            gamma_n_tf,
        )

        return float(loss.numpy())

    def compute_td_loss(
        self,
        state: np.ndarray,
        action: int,
        reward: float,
        next_state: np.ndarray,
        done: bool,
    ) -> float:
        """Compute TD loss for a single transition (useful for inference/evaluation).

        Parameters
        ----------
        state : np.ndarray
            Current state.
        action : int
            Action taken.
        reward : float
            Reward received.
        next_state : np.ndarray
            Next state.
        done : bool
            Whether episode ended.

        Returns
        -------
        td_loss : float
            The TD error (loss) for this transition.
        """
        # Convert to tensors
        state_tensor = tf.constant(state[np.newaxis], dtype=tf.float32)
        next_state_tensor = tf.constant(next_state[np.newaxis], dtype=tf.float32)

        # Get Q-values
        q_values = self.action_model(state_tensor, training=False)
        next_q_values = self.target_model(next_state_tensor, training=False)

        # Compute target
        max_next_q = tf.reduce_max(next_q_values, axis=1)
        target = reward + (0.0 if done else self.gamma) * max_next_q

        # Compute TD error
        current_q = q_values[0][action]
        td_error = target - current_q
        td_loss = td_error**2

        return float(td_loss.numpy())

    def save(self, directory: str):
        """Save agent state: models, epsilon, and parameters."""
        os.makedirs(directory, exist_ok=True)
        self.action_model.save(os.path.join(directory, "action_model.keras"))
        self.target_model.save(os.path.join(directory, "target_model.keras"))

        np.savez(
            os.path.join(directory, "agent_state.npz"),
            epsilon=self.epsilon,
            epsilon_max=self.epsilon_max,
            epsilon_min=self.epsilon_min,
            epsilon_decay=self.epsilon_decay,
            gamma=self.gamma,
            batch_size=self.batch_size,
            step_per_update=self.step_per_update,
            step_per_update_target_model=self.step_per_update_target_model,
            moving_avg_window_size=self.moving_avg_window_size,
            moving_avg_stop_thr=self.moving_avg_stop_thr,
            n_step=self.n_step,
        )

    def load(self, directory: str):
        """Load agent state: models, epsilon, and parameters."""
        from keras.models import load_model

        self.action_model = load_model(os.path.join(directory, "action_model.keras"))
        self.target_model = load_model(os.path.join(directory, "target_model.keras"))

        # Recreate TensorFlow functions after loading models
        self._create_tf_functions()

        data = np.load(os.path.join(directory, "agent_state.npz"))
        self.epsilon = float(data["epsilon"])
        self.epsilon_max = float(data["epsilon_max"])
        self.epsilon_min = float(data["epsilon_min"])
        self.epsilon_decay = float(data["epsilon_decay"])
        self.gamma = float(data["gamma"])
        self.batch_size = int(data["batch_size"])
        self.step_per_update = int(data["step_per_update"])
        self.step_per_update_target_model = int(data["step_per_update_target_model"])
        self.moving_avg_window_size = int(data["moving_avg_window_size"])
        self.moving_avg_stop_thr = float(data["moving_avg_stop_thr"])
        self.n_step = int(data.get("n_step", 5))
