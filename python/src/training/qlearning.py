import numpy as np
from tqdm import trange

from agent.qagent import QAgent
from utils.log import Logger

logger = Logger(__name__)


class QLearning:
    """
    Implementation of the Q-Learning algorithm for OpenAI Gymnasium environments.

    Parameters
    ----------
    env : gym.Env
        The environment in which the agent interacts.
    agent : QAgent
        The Q-learning agent.
    episode_count : int, optional (default=2000)
        Total number of training episodes.
    episode_max_steps : int, optional (default=200)
        Maximum number of steps per episode.

    Attributes
    ----------
    learning_history : list
        List to store the history of learning episodes if record_history is enabled.
    """

    def __init__(
        self,
        env,
        agent: QAgent,
        episode_count: int = 2000,
        episode_max_steps: int = 200,
    ):
        self.env = env
        self.agent = agent
        self.episode_count = episode_count
        self.episode_max_steps = episode_max_steps

        self.learning_history = []

    def train(self, record_history: bool = False):
        """Trains the agent using the Q-learning algorithm over a specified number of episodes and optionally records
        the learning history.

        Parameters
        ----------
        record_history: bool
            Optionally records the learning history.
        """
        rewards_per_episode = []

        for _ep in trange(self.episode_count, desc="Training", unit="ep"):
            state, _ = self.env.reset()
            done = False
            total_reward = 0
            episode_history = [] if record_history else None
            step_count = 0

            while not done and step_count < self.episode_max_steps:
                action = self.agent.choose_action(state)
                next_state, reward, terminated, truncated, _ = self.env.step(action)
                done = terminated or truncated

                self.agent.update_q(state, action, reward, next_state, done)

                total_reward += reward
                frame = self.env.render() if self.env.render_mode == "ansi" else None

                if record_history:
                    episode_history.append(
                        {
                            "frame": frame,
                            "state": state,
                            "action": action,
                            "reward": reward,
                            "total_reward": total_reward,
                            "next_state": next_state,
                            "done": done,
                        }
                    )

                state = next_state
                step_count += 1

            self.agent.decay_epsilon()

            rewards_per_episode.append(total_reward)
            if record_history:
                self.learning_history.append(episode_history)

        return rewards_per_episode

    def evaluate(self, test_episode_count: int = 1000):
        """Evaluates the trained agent over a specified number of episodes, returning the average number of steps and total reward.

        Parameters
        ----------
        test_episode_count : int, optional (default=1000)
            Number of episodes to run for evaluation.
        Returns
        -------
        avg_steps : float
            Average number of steps taken per episode during evaluation.
        avg_total_reward : float
            Average total reward received per episode during evaluation.
        """

        if np.all(self.agent.Q == 0):
            raise ValueError(
                "Cannot evaluate agent before training. Please call train() first."
            )

        sum_steps = 0
        sum_total_reward = 0

        for _ in range(test_episode_count):
            state, _ = self.env.reset()
            total_reward = 0
            step_count = 0
            done = False

            while not done and step_count < self.episode_max_steps:
                action = np.argmax(self.agent.Q[state])
                next_state, reward, terminated, truncated, _ = self.env.step(action)
                done = terminated or truncated

                total_reward += reward
                state = next_state
                step_count += 1

            sum_steps += step_count
            sum_total_reward += total_reward

        avg_steps = sum_steps / test_episode_count
        avg_total_reward = sum_total_reward / test_episode_count

        logger.info(f"Average number of steps per episode: {avg_steps:.1f}")
        logger.info(f"Average total reward per episode: {avg_total_reward:.1f}")

        return avg_steps, avg_total_reward
