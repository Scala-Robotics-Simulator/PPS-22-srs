import statistics
import time

import numpy as np
import pygame
from tqdm import trange

from agent.scala_dqagent import DQAgent
from utils.log import Logger

logger = Logger(__name__)


class DQLearning:
    """
    Implementation of the Deep Q-Learning algorithm for OpenAI Gymnasium environments.

    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    agent : DQAgent
        The Deep Q-Agent.
    episode_count : int, optional (default=1000)
        Number of training episodes.
    episode_max_steps : int, optional (default=200)
        Maximum number of steps per episode.
    """

    def __init__(
        self,
        env,
        agent: DQAgent,
        episode_count: int = 1000,
        episode_max_steps: int = 200,
    ):
        self.env = env
        self.agent = agent
        self.episode_count = episode_count
        self.episode_max_steps = episode_max_steps

    def simple_dqn_training(self):
        """Trains the agent using the Deep Q-Learning algorithm."""
        train_rewards = []
        train_step_count = 0

        for n in trange(self.episode_count, desc="Training DQN", unit="ep"):
            states, _ = self.env.reset()
            state = states[self.agent.id]
            episode_reward = 0
            episode_start_time = time.time()
            episode_epsilon = self.agent.epsilon
            done = False
            step_count = 0

            while step_count < self.episode_max_steps and not done:
                action = self.agent.choose_action(state)
                actions = {self.agent.id: action}

                next_states, rewards, terminateds, truncateds, _ = self.env.step(
                    actions
                )
                next_state = next_states[self.agent.id]
                reward = rewards[self.agent.id]
                done = terminateds[self.agent.id] or truncateds[self.agent.id]

                self.agent.store_transition(state, action, reward, next_state, done)
                state = next_state

                if (
                    train_step_count % self.agent.step_per_update == 0
                    and len(self.agent.replay_memory) >= self.agent.batch_size
                ):
                    self.agent.dqn_update()

                if train_step_count % self.agent.step_per_update_target_model == 0:
                    self.agent.update_target_model()

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

            logger.info(
                f"Episode: {n} | Steps: {step_count}[{train_step_count}] | "
                f"Epsilon: {episode_epsilon:.3f} | Time: {episode_time:.2f}s | "
                f"Reward: {episode_reward:.1f} | MovingAvg: {moving_avg_reward:.1f}"
            )

            # if (
            #     self.agent.moving_avg_stop_thr
            #     and moving_avg_reward >= self.agent.moving_avg_stop_thr
            # ):
            #     break

        return train_rewards

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
            states, _ = self.env.reset()
            state = states[self.agent.id]
            done = False
            total_reward = 0

            while not done and running:
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        running = False

                q_values = self.agent.action_model.predict(state[np.newaxis], verbose=0)
                action = np.argmax(q_values[0])
                actions = {self.agent.id: action}

                next_states, rewards, terminateds, truncateds, _ = self.env.step(
                    actions
                )
                next_state = next_states[self.agent.id]
                reward = rewards[self.agent.id]
                done = terminateds[self.agent.id] or truncateds[self.agent.id]
                total_reward += reward
                state = next_state

                rgb_array = self.env.render()
                surface = pygame.surfarray.make_surface(
                    np.transpose(rgb_array, (1, 0, 2))
                )
                surface = pygame.transform.scale(surface, render_scale)
                screen.blit(surface, (0, 0))
                pygame.display.flip()

                clock.tick(fps)

            logger.info(f"Episode {ep + 1}/{episodes} - Reward: {total_reward}")

        self.env.close()
        pygame.quit()
