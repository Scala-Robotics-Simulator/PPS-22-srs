import logging

import numpy as np
import pygame
from tqdm import trange

from agent.qagent import QAgent

logger = logging.getLogger(__name__)


class QLearning:
    """
    Implementation of the Q-Learning algorithm.

    Parameters
    ----------
    env : Env
        The environment in which the agent interacts.
    agents : dict[str, QAgent]
        Dictionary mapping agent IDs to Q-learning agent instances.
    episode_count : int, optional (default=2000)
        Total number of training episodes.
    max_steps_per_episode : int, optional (default=200)
        Maximum number of steps per episode.

    Attributes
    ----------
    learning_history : list
        List to store the history of learning episodes if record_history is enabled.
    """

    def __init__(
        self,
        env,
        agents: dict[str, QAgent],
        episode_count: int = 2000,
        max_steps_per_episode: int = 200,
    ):
        self.env = env
        self.agents = agents
        self.episode_count = episode_count
        self.episode_max_steps = max_steps_per_episode

        self.learning_history = {agent_id: [] for agent_id in agents.keys()}

    async def train(self, render: bool = False, record_history: bool = True):
        """Trains the agents using the Q-learning algorithm over a specified number of episodes, optionally rendering the
        environment and recording the learning history.

        Parameters
        ----------
        render: bool (default=False)
            Whether to render the environment during training.
        record_history: bool (default=True)
            Optionally records the learning history.
        """
        rewards_per_agent = {agent_id: [] for agent_id in self.agents.keys()}
        running = True

        if render:
            pygame.init()
            screen = pygame.display.set_mode((800, 600))
            pygame.display.set_caption("Q-Learning Multi-Agent")
            clock = pygame.time.Clock()

        for _ep in trange(self.episode_count, desc="Training", unit="ep"):
            obs, _ = await self.env.reset()
            done = dict.fromkeys(self.agents.keys(), False)
            total_reward = dict.fromkeys(self.agents.keys(), 0)
            episode_history = (
                {agent_id: [] for agent_id in self.agents.keys()}
                if record_history
                else None
            )
            step_count = 0

            while not all(done.values()) and step_count < self.episode_max_steps:
                actions = {
                    k: self.agents[k].choose_action(v, epsilon_greedy=True)
                    for k, v in obs.items()
                }

                next_obs, rewards, terminateds, truncateds, _ = await self.env.step(
                    actions
                )

                for agent_id in self.agents.keys():
                    if (
                        agent_id not in obs
                        or agent_id not in next_obs
                        or agent_id not in rewards
                    ):
                        continue

                    agent_done = terminateds.get(agent_id, False) or truncateds.get(
                        agent_id, False
                    )
                    self.agents[agent_id].update_q(
                        obs[agent_id],
                        actions[agent_id],
                        rewards[agent_id],
                        next_obs[agent_id],
                        agent_done,
                    )
                    total_reward[agent_id] += rewards[agent_id]
                    done[agent_id] = agent_done

                    if record_history:
                        episode_history[agent_id].append(
                            {
                                "state": obs[agent_id],
                                "action": actions[agent_id],
                                "reward": rewards[agent_id],
                                "total_reward": total_reward[agent_id],
                                "next_state": next_obs[agent_id],
                                "step": step_count,
                                "done": agent_done,
                            }
                        )

                obs = next_obs
                step_count += 1

                if render:
                    rgb_array = await self.env.render()
                    surface = pygame.surfarray.make_surface(
                        np.transpose(rgb_array, (1, 0, 2))
                    )
                    screen.blit(surface, (0, 0))
                    pygame.display.flip()
                    clock.tick(60)

                    for event in pygame.event.get():
                        if event.type == pygame.QUIT:
                            running = False

                if not running:
                    break

            for agent_id, agent in self.agents.items():
                agent.decay_epsilon(_ep)
                rewards_per_agent[agent_id].append(total_reward[agent_id])

                if record_history:
                    self.learning_history[agent_id].append(
                        {
                            "steps": step_count
                            if done[agent_id]
                            else self.episode_max_steps,
                            "total_reward": total_reward[agent_id],
                        }
                    )

        if render:
            pygame.quit()

        return rewards_per_agent

    async def evaluate(self, test_episode_count: int = 100, render: bool = False):
        """Evaluates the trained agents over a given number of test episodes.

        Parameters
        ----------
        test_episode_count: int (default=100)
            Number of evaluation episodes.
        render: bool (default=False)
            Whether to render the environment during evaluation.
        """
        results = {}
        running = True

        for agent_id, agent in self.agents.items():
            if np.all(agent.Q == 0):
                raise ValueError(f"Cannot evaluate agent {agent_id} before training.")

        if render:
            pygame.init()
            screen = pygame.display.set_mode((800, 600))
            pygame.display.set_caption("Q-Learning Multi-Agent Evaluation")
            clock = pygame.time.Clock()

        for _ep in trange(test_episode_count, desc="Evaluation", unit="ep"):
            obs, _ = await self.env.reset()
            done = dict.fromkeys(self.agents.keys(), False)
            total_reward = dict.fromkeys(self.agents.keys(), 0)
            step_count = 0

            while not all(done.values()) and step_count < self.episode_max_steps:
                actions = {
                    k: self.agents[k].choose_action(v, epsilon_greedy=False)
                    for k, v in obs.items()
                }

                next_obs, rewards, terminateds, truncateds, _ = await self.env.step(
                    actions
                )

                for agent_id in self.agents.keys():
                    if (
                        agent_id not in obs
                        or agent_id not in next_obs
                        or agent_id not in rewards
                    ):
                        continue

                    agent_done = terminateds.get(agent_id, False) or truncateds.get(
                        agent_id, False
                    )
                    total_reward[agent_id] += rewards[agent_id]
                    done[agent_id] = agent_done

                obs = next_obs
                step_count += 1

                if render:
                    rgb_array = await self.env.render()
                    surface = pygame.surfarray.make_surface(
                        np.transpose(rgb_array, (1, 0, 2))
                    )
                    screen.blit(surface, (0, 0))
                    pygame.display.flip()
                    clock.tick(60)

                    for event in pygame.event.get():
                        if event.type == pygame.QUIT:
                            running = False
                    if not running:
                        break

            for agent_id in self.agents.keys():
                if agent_id not in results:
                    results[agent_id] = {"total_reward": 0.0, "avg_steps": 0.0}
                results[agent_id]["total_reward"] += total_reward[agent_id]
                results[agent_id]["avg_steps"] += step_count

        for agent_id, data in results.items():
            data["avg_total_reward"] = data["total_reward"] / test_episode_count
            data["avg_steps"] = data["avg_steps"] / test_episode_count
            logger.info(
                f"[{agent_id}] Avg steps: {data['avg_steps']:.1f} | Avg reward: {data['avg_total_reward']:.1f}"
            )

        if render:
            pygame.quit()

        return results
