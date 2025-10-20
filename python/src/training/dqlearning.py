import time, statistics, numpy as np
from tqdm import trange
import pygame

from agent.dqagent import DQAgent
from training.dqnetwork import DQNetwork


class DQLearning:
    """
    Implementation of the Deep Q-Learning algorithm for OpenAI Gymnasium environments.

    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    agent : DQAgent
        The Deep Q-Agent.
    dqn_action_model : DQNetwork
        The main Q-network, trained at each step.
    dqn_target_model : DQNetwork
        The target Q-network, periodically updated from the main model.
    episode_count : int, optional (default=1000)
        Number of training episodes.
    episode_max_steps : int, optional (default=200)
        Maximum number of steps per episode.
    """

    def __init__(
        self,
        env,
        agent: DQAgent,
        dqn_action_model: DQNetwork,
        dqn_target_model: DQNetwork,
        episode_count: int = 1000,
        episode_max_steps: int = 200,
    ):
        self.env = env
        self.agent = agent
        self.dqn_action_model = dqn_action_model.model
        self.dqn_target_model = dqn_target_model.model
        self.episode_count = episode_count
        self.episode_max_steps = episode_max_steps

    def simple_dqn_training(self):
        """Trains the agent using the Deep Q-Learning algorithm."""
        train_rewards = []
        train_step_count = 0

        for n in trange(self.episode_count, desc="Training DQN", unit="ep"):
            state, _ = self.env.reset()
            episode_reward = 0
            episode_start_time = time.time()
            episode_epsilon = self.agent.epsilon
            done = False
            step_count = 0

            while step_count < self.episode_max_steps and not done:
                action = self.agent.choose_action(state, self.dqn_action_model)

                next_state, reward, terminated, truncated, _ = self.env.step(action)
                done = terminated or truncated

                self.agent.store_transition(state, action, reward, next_state, done)
                state = next_state

                if (
                    train_step_count % self.agent.step_per_update == 0
                    and len(self.agent.replay_memory) >= self.agent.batch_size
                ):
                    mini_batch = self.agent.get_random_batch()
                    self.dqn_action_model = self.simple_dqn_update(
                        self.dqn_action_model,
                        self.dqn_target_model,
                        mini_batch,
                        self.agent.gamma,
                    )

                if train_step_count % self.agent.step_per_update_target_model == 0:
                    self.agent.update_target_model(
                        self.dqn_action_model, self.dqn_target_model
                    )

                self.agent.decay_epsilon()

                step_count += 1
                train_step_count += 1
                episode_reward += reward

            episode_time = time.time() - episode_start_time
            moving_avg_reward = (
                statistics.mean(train_rewards[-self.agent.moving_avg_window_size :])
                if len(train_rewards) >= self.agent.moving_avg_window_size
                else episode_reward
            )
            train_rewards.append(episode_reward)

            print(
                f"Episode: {n} | Steps: {step_count}[{train_step_count}] | "
                f"Epsilon: {episode_epsilon:.3f} | Time: {episode_time:.2f}s | "
                f"Reward: {episode_reward:.1f} | MovingAvg: {moving_avg_reward:.1f}"
            )

            if (
                self.agent.moving_avg_stop_thr
                and moving_avg_reward >= self.agent.moving_avg_stop_thr
            ):
                break

        return train_rewards

    def simple_dqn_update(
        self,
        dqn_action_model,
        dqn_target_model,
        mini_batch: tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray, np.ndarray],
        gamma: float,
    ):
        """Update the action model using a mini-batch from replay memory.

        Parameters
        ----------
        dqn_action_model : keras.Sequential
            The main Deep Q-network to be updated.
        dqn_target_model : keras.Sequential
            The target Deep Q-network used to compute target Q-values.
        mini_batch : tuple of np.ndarray
            A mini-batch of transitions (states, actions, rewards, new_states, dones).
        gamma : float
            Discount factor for future rewards.

        Returns
        -------
        dqn_action_model : keras.Sequential
            The updated action model.
        """
        # the transition mini-batch is divided into a mini-batch for each element of a transition
        state_batch, action_batch, reward_batch, new_state_batch, done_batch = (
            mini_batch
        )

        # 1. find the target model Q values for all possible actions given the new state batch
        target_new_state_q_values = dqn_target_model.predict(new_state_batch, verbose=0)

        # 2. find the action model Q values for all possible actions given the current state batch
        predicted_state_q_values = dqn_action_model.predict(state_batch, verbose=0)

        # estimate the target values y_i
        # for the action we took, use the target model Q values
        # for other actions, use the action model Q values
        # in this way, loss function will be 0 for other actions
        for i, (a, r, new_state_q_values, done) in enumerate(
            zip(action_batch, reward_batch, target_new_state_q_values, done_batch)
        ):
            if not done:
                target_value = r + gamma * np.amax(new_state_q_values)
            else:
                target_value = r
            predicted_state_q_values[i][a] = target_value  # y_i

        # 3. update weights of action model using the train_on_batch method
        dqn_action_model.train_on_batch(state_batch, predicted_state_q_values)

        # return the updated action model
        return dqn_action_model

    def play_with_pygame(self, episodes=5, fps=30, render_scale=(600, 400)):
        """Run the trained agent and visualize with Pygame.

        Parameters
        ----------
        episodes : int, optional (default=5)
            Number of episodes to play.
        fps : int, optional (default=30)
            Frames per second for rendering.
        render_scale : tuple, optional (default=(600, 400))
            Scale of the rendering window.
        """
        pygame.init()
        screen = pygame.display.set_mode(render_scale)
        pygame.display.set_caption("DQN Agent Playing")
        clock = pygame.time.Clock()
        running = True

        for ep in range(episodes):
            state, _ = self.env.reset()
            done = False
            total_reward = 0

            while not done and running:
                # Handle quit events
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        running = False

                q_values = self.dqn_action_model.predict(state[np.newaxis], verbose=0)
                action = np.argmax(q_values[0])

                # Step environment
                next_state, reward, terminated, truncated, _ = self.env.step(action)
                done = terminated or truncated
                total_reward += reward
                state = next_state

                # Render RGB array
                rgb_array = self.env.render()  # (H, W, 3)
                surface = pygame.surfarray.make_surface(
                    np.transpose(rgb_array, (1, 0, 2))
                )
                surface = pygame.transform.scale(surface, render_scale)
                screen.blit(surface, (0, 0))
                pygame.display.flip()

                clock.tick(fps)

            print(f"Episode {ep + 1}/{episodes} - Reward: {total_reward}")

        self.env.close()
        pygame.quit()
