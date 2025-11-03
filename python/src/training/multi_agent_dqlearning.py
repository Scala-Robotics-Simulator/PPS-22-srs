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
        configs: list[str],
        episode_count: int = 1000,
        episode_max_steps: int = 200,
    ):
        self.env = env
        self.agents = agents
        self.configs = configs
        self.episode_count = episode_count
        self.episode_max_steps = episode_max_steps

    def simple_dqn_training(self, checkpoint_base: str | None = None):
        """Trains the agent using the Deep Q-Learning algorithm."""
        train_rewards = []
        train_step_count = 0
        max_avg_reward = np.finfo(np.float32).min

        for n in trange(self.episode_count, desc="Training DQN", unit="ep"):
            config = np.random.choice(self.configs)
            _ = self.env.init(config)
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

                # Process each agent
                for agent in self.agents:
                    if not agent.terminated:
                        # Store transition
                        agent.store_transition(
                            states[agent.id],
                            actions[agent.id],
                            rewards[agent.id],
                            next_states[agent.id],
                            dones[agent.id],
                        )

                        episode_reward[agent.id] += rewards[agent.id]

                        if dones[agent.id]:
                            agent.terminated = True

                        if (
                            train_step_count % agent.step_per_update == 0
                            and len(agent.replay_memory) >= agent.batch_size
                        ):
                            agent.dqn_update()

                        if train_step_count % agent.step_per_update_target_model == 0:
                            agent.update_target_model()

                        agent.decay_epsilon(n)

                done = all(dones.values())
                states = next_states
                step_count += 1
                train_step_count += 1

            episode_time = time.time() - episode_start_time
            moving_avg_reward = {
                agent.id: (
                    statistics.mean(
                        [
                            reward[agent.id]
                            for reward in train_rewards[-agent.moving_avg_window_size :]
                        ]
                    )
                    if len(train_rewards) >= agent.moving_avg_window_size
                    else max_avg_reward
                )
                for agent in self.agents
            }
            train_rewards.append(episode_reward)

            logger.info(
                f"Episode: {n} | Steps: {step_count}[{train_step_count}] | "
                f"Epsilon: {episode_epsilon:.3f} | Time: {episode_time:.2f}s | "
                f"Reward: {episode_reward} | MovingAvg: {moving_avg_reward}"
            )
            if checkpoint_base is not None:
                for agent in self.agents:
                    if moving_avg_reward[agent.id] > max_avg_reward:
                        max_avg_reward = moving_avg_reward[agent.id]
                        save_path = f"{checkpoint_base}_ep{n + 1}"
                        agent.save(save_path)
                        logger.info(
                            f"\n[Checkpoint] Saved at episode {n + 1} | Reward: {train_rewards[n][agent.id]:.3f} | AvgReward: {max_avg_reward:.3f}"
                        )
            # if (
            #     self.agent.moving_avg_stop_thr
            #     and moving_avg_reward >= self.agent.moving_avg_stop_thr
            # ):
            #     break
        if checkpoint_base is not None:
            for agent in self.agents:
                save_path = f"{checkpoint_base}_final"
                agent.save(save_path)
            logger.info("\n[Final Save] Training complete.")

        return train_rewards

    def play_with_pygame(self, episodes=1, fps=30, render_scale=(800, 600)):
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
        paused = False
        current_fps = fps
        # Font for displaying info
        font = pygame.font.Font(None, 24)
        info_font = pygame.font.Font(None, 20)

        for ep in range(episodes):
            states, _ = self.env.reset()
            done = False
            total_reward = 0
            for agent in self.agents:
                agent.terminated = False

            step = 0
            while not done and running:
                step += 1
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        running = False
                    elif event.type == pygame.KEYDOWN:
                        if event.key == pygame.K_ESCAPE or event.key == pygame.K_q:
                            running = False
                        elif event.key == pygame.K_SPACE:
                            paused = not paused
                        elif event.key == pygame.K_UP:
                            current_fps = min(240, current_fps + 10)
                        elif event.key == pygame.K_DOWN:
                            current_fps = max(10, current_fps - 10)

                # Skip step if paused
                if paused:
                    pygame.time.wait(100)
                    continue
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
                # Display info overlay
                if font and info_font:
                    info_texts = [
                        f"Episode: {ep + 1}",
                        f"Step: {step}",
                        f"Reward: {total_reward:.2f}",
                        f"Epsilon: {self.agents[0].epsilon:.4f}",
                        f"FPS: {current_fps} (↑/↓ to adjust)",
                        f"{'PAUSED' if paused else 'SPACE: Pause'}",
                    ]

                    y_offset = 10
                    for text in info_texts:
                        color = (255, 255, 0) if paused else (255, 255, 255)
                        text_surface = info_font.render(text, True, color, (0, 0, 0))
                        screen.blit(text_surface, (10, y_offset))
                        y_offset += 25

                pygame.display.flip()

                clock.tick(current_fps)

            logger.info(f"Episode {ep + 1}/{episodes} - Reward: {total_reward}")

        pygame.quit()
