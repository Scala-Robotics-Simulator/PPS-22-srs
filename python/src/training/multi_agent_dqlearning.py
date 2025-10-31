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
        agents: list[DQAgent],
        episode_count: int = 1000,
        episode_max_steps: int = 200,
    ):
        self.env = env
        self.agents = agents
        self.episode_count = episode_count
        self.episode_max_steps = episode_max_steps

    def simple_dqn_training(self):
        """Trains the agent using the Deep Q-Learning algorithm."""
        train_rewards = []
        train_step_count = 0

        for n in trange(self.episode_count, desc="Training DQN", unit="ep"):
            states, _ = self.env.reset()
            episode_reward = {agent.id: 0 for agent in self.agents}
            episode_start_time = time.time()
            episode_epsilon = self.agents[0].epsilon
            done = False
            step_count = 0
            for agent in self.agents:
                agent.terminated = False

            while step_count < self.episode_max_steps and not done:
                actions = {
                    agent.id: agent.choose_action(states[agent.id])
                    for agent in self.agents
                    if not agent.terminated
                }

                next_states, rewards, terminateds, truncateds, _ = self.env.step(
                    actions
                )
                dones = {
                    agent.id: terminateds[agent.id] or truncateds[agent.id]
                    for agent in self.agents
                }
                for agent in self.agents:
                    if dones[agent.id]:
                        agent.terminated = True
                done = all(dones.values())
                for agent in self.agents:
                    if not agent.terminated:
                        agent.store_transition(
                            states[agent.id],
                            actions[agent.id],
                            rewards[agent.id],
                            next_states[agent.id],
                            dones[agent.id],
                        )

                states = next_states

                for agent in self.agents:
                    if not agent.terminated:
                        if (
                            train_step_count % agent.step_per_update == 0
                            and len(agent.replay_memory) >= agent.batch_size
                        ):
                            agent.dqn_update()

                        if train_step_count % agent.step_per_update_target_model == 0:
                            agent.update_target_model()

                        agent.decay_epsilon(n)

                step_count += 1
                train_step_count += 1
                for agent in self.agents:
                    if not agent.terminated:
                        episode_reward[agent.id] += rewards[agent.id]

            episode_time = time.time() - episode_start_time
            moving_avg_reward = {
                agent.id: (
                    statistics.mean(train_rewards[-agent.moving_avg_window_size :])
                    if len(train_rewards) >= agent.moving_avg_window_size
                    else episode_reward[agent.id]
                )
                for agent in self.agents
                if not agent.terminated
            }
            train_rewards.append(episode_reward)

            logger.info(
                f"Episode: {n} | Steps: {step_count}[{train_step_count}] | "
                f"Epsilon: {episode_epsilon:.3f} | Time: {episode_time:.2f}s | "
                f"Reward: {episode_reward} | MovingAvg: {moving_avg_reward}"
            )

            # if (
            #     self.agent.moving_avg_stop_thr
            #     and moving_avg_reward >= self.agent.moving_avg_stop_thr
            # ):
            #     break

        return train_rewards

    def play_with_pygame(self, episodes=5, fps=30, render_scale=(800, 600)):
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
            done = False
            total_reward = 0
            for agent in self.agents:
                agent.terminated = False

            while not done and running:
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        running = False

                actions = {
                    agent.id: np.argmax(
                        agent.action_model.predict(
                            states[agent.id][np.newaxis], verbose=0
                        )[0]
                    )
                    for agent in self.agents
                }

                next_states, rewards, terminateds, truncateds, _ = self.env.step(
                    actions
                )
                dones = {
                    agent.id: terminateds[agent.id] or truncateds[agent.id]
                    for agent in self.agents
                }
                done = all(dones.values())
                total_reward += rewards[self.agents[0].id]
                states = next_states

                rgb_array = self.env.render()
                surface = pygame.surfarray.make_surface(
                    np.transpose(rgb_array, (1, 0, 2))
                )
                surface = pygame.transform.scale(surface, render_scale)
                screen.blit(surface, (0, 0))
                pygame.display.flip()

                clock.tick(fps)

            logger.info(f"Episode {ep + 1}/{episodes} - Reward: {total_reward}")

        pygame.quit()
