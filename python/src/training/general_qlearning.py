import numpy as np
import pygame
from agent.qagent import QAgent
from pygame import Surface
from pygame.time import Clock
from tqdm import trange
from utils.log import Logger

logger = Logger(__name__)


class QLearning:
    """
    Implementation of a multi-agent Q-Learning algorithm.

    Parameters
    ----------
    env : Env
        The environment in which the agents interact.
    agents : dict[str, QAgent]
        A dictionary mapping agent IDs to their respective Q-learning agents.
    episode_count : int, optional (default=2000)
        Total number of training episodes.
    max_steps_per_episode : int, optional (default=200)
        Maximum number of steps per episode before it is truncated.

    Attributes
    ----------
    learning_history : dict[str, list[dict]]
        Per-agent history of learning metrics (steps and total rewards)
        recorded after each episode when `record_history=True`.
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

    def _run_episode(
        self,
        render: bool,
        record_history: bool,
        episode_idx: int,
        training: bool = True,
        screen=None,
        clock=None,
    ) -> tuple[dict[str, float], int, bool, dict | None]:
        """Runs a single training or evaluation episode.

        Parameters
        ----------
        render : bool
            If True, renders each frame of the environment using Pygame.
        record_history : bool
            If True, records the episode history for each agent.
        episode_idx : int
            Index of the current episode (used for epsilon decay).
        training : bool, optional (default=True)
            If True, performs Q-value updates and epsilon decay.
            If False, only evaluates the policy without updates.
        screen : pygame.Surface, optional
            The Pygame surface to use for rendering (if enabled).
        clock : pygame.time.Clock, optional
            The Pygame clock for frame rate control.

        Returns
        -------
        tuple[dict[str, float], int, bool, dict | None]
            A tuple containing:
            - total_reward : dict[str, float]
                Total accumulated reward per agent.
            - step_count : int
                Number of steps executed during the episode.
            - running : bool
                Whether the user closed the window (False to stop training).
            - episode_history : dict | None
                Step-by-step episode data if `record_history=True`.
        """
        obs, _ = self.env.reset()
        done = dict.fromkeys(self.agents.keys(), False)
        total_reward = dict.fromkeys(self.agents.keys(), 0)
        episode_history = (
            {agent_id: [] for agent_id in self.agents.keys()}
            if record_history
            else None
        )
        running = True
        step_count = 0

        while not all(done.values()) and step_count < self.episode_max_steps:
            actions = {
                k: self.agents[k].choose_action(v, epsilon_greedy=training)
                for k, v in obs.items()
            }

            next_obs, rewards, terminateds, truncateds, _ = self.env.step(actions)

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
                done[agent_id] = agent_done
                total_reward[agent_id] += rewards[agent_id]

                if training:
                    self.agents[agent_id].update_q(
                        obs[agent_id],
                        actions[agent_id],
                        rewards[agent_id],
                        next_obs[agent_id],
                        agent_done,
                    )

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
                rgb_array = self.env.render()
                _render_frame(rgb_array, screen, clock)
                running = _check_quit_event()
                if not running:
                    break

        if training:
            for agent_id in self.agents.keys():
                self.agents[agent_id].decay_epsilon(episode_idx)
                if record_history:
                    self.learning_history[agent_id].append(
                        {
                            "steps": step_count
                            if done[agent_id]
                            else self.episode_max_steps,
                            "total_reward": total_reward[agent_id],
                        }
                    )

        return total_reward, step_count, running, episode_history

    def train(
        self, render: bool = False, record_history: bool = True
    ) -> dict[str, list[float]]:
        """
        Trains the agents using the Q-learning algorithm.

        Runs a specified number of episodes, updating Q-values
        and optionally rendering and recording performance history.

        Parameters
        ----------
        render : bool, optional (default=False)
            Whether to visually render the environment using Pygame.
        record_history : bool, optional (default=True)
            Whether to record per-episode learning statistics.

        Returns
        -------
        dict[str, list[float]]
            Per-agent list of total rewards for each episode.
        """
        rewards_per_agent = {agent_id: [] for agent_id in self.agents.keys()}
        _running = True

        screen, clock = (None, None)
        if render:
            screen, clock = _init_render("Q-Learning Multi-Agent Training")

        for ep in trange(self.episode_count, desc="Training", unit="ep"):
            total_reward, step_count, _running, _ = self._run_episode(
                render, record_history, ep, training=True, screen=screen, clock=clock
            )
            for agent_id in self.agents.keys():
                rewards_per_agent[agent_id].append(total_reward[agent_id])
            if not _running:
                break

        if render:
            pygame.quit()

        return rewards_per_agent

    def evaluate(
        self,
        test_episode_count: int = 100,
        max_steps_per_episode: int or None = None,
        render: bool = False,
    ) -> dict[str, dict[str, float]]:
        """
        Evaluates the trained agents without further learning.

        Runs a series of evaluation episodes where agents act greedily
        (without exploration) and computes average rewards and steps.

        Parameters
        ----------
        test_episode_count : int, optional (default=100)
            Number of episodes to evaluate.
        max_steps_per_episode : int, optional
            Overrides the default maximum number of steps per episode used
            during evaluation. If None, uses the training value
            (`self.episode_max_steps`).
        render : bool, optional (default=False)
            Whether to visually render the environment using Pygame.

        Returns
        -------
        dict[str, dict[str, float]]
            A dictionary mapping each agent ID to its evaluation results:
            - "avg_total_reward": Average cumulative reward.
            - "avg_steps": Average number of steps per episode.
        """
        results = {
            aid: {"total_reward": 0.0, "avg_steps": 0.0} for aid in self.agents.keys()
        }

        for agent_id, agent in self.agents.items():
            if np.all(agent.Q == 0):
                raise ValueError(f"Cannot evaluate agent {agent_id} before training.")

        original_max_steps = self.episode_max_steps
        if max_steps_per_episode is not None:
            self.episode_max_steps = max_steps_per_episode

        screen, clock = (None, None)
        if render:
            screen, clock = _init_render("Q-Learning Multi-Agent Evaluation")

        _running = True
        for ep in trange(test_episode_count, desc="Evaluation", unit="ep"):
            total_reward, step_count, _running, _ = self._run_episode(
                render,
                record_history=False,
                episode_idx=ep,
                training=False,
                screen=screen,
                clock=clock,
            )
            for agent_id in self.agents.keys():
                results[agent_id]["total_reward"] += total_reward[agent_id]
                results[agent_id]["avg_steps"] += step_count
            if not _running:
                break

        self.episode_max_steps = original_max_steps

        for agent_id, data in results.items():
            data["avg_total_reward"] = data["total_reward"] / test_episode_count
            data["avg_steps"] = data["avg_steps"] / test_episode_count
            logger.info(
                f"[{agent_id}] Avg steps: {data['avg_steps']:.1f} | "
                f"Avg reward: {data['avg_total_reward']:.1f}"
            )

        if render:
            pygame.quit()

        return results


def _init_render(title: str) -> tuple[Surface, Clock]:
    """Initializes a Pygame window and returns the screen and clock.

    Parameters
    ----------
    title : str
        Title of the Pygame window.

    Returns
    -------
    tuple[pygame.Surface, pygame.time.Clock]
        The initialized screen surface and Pygame clock.
    """
    pygame.init()
    screen = pygame.display.set_mode((800, 600))
    pygame.display.set_caption(title)
    clock = pygame.time.Clock()
    return screen, clock


def _render_frame(rgb_array: np.ndarray, screen: Surface, clock: Clock) -> None:
    """Renders a single frame using Pygame.

    Parameters
    ----------
    rgb_array : np.ndarray
        The RGB image array returned by the environment.
    screen : pygame.Surface
        The Pygame display surface to render onto.
    clock : pygame.time.Clock
        The Pygame clock used to control the frame rate.
    """
    surface = pygame.surfarray.make_surface(np.transpose(rgb_array, (1, 0, 2)))
    screen.blit(surface, (0, 0))
    pygame.display.flip()
    clock.tick(60)


def _check_quit_event() -> bool:
    """Checks if a quit event was triggered in the Pygame window.

    Returns
    -------
    bool
        False if a quit event (window close) was detected, True otherwise.
    """
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            return False
    return True
