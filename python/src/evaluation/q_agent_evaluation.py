from collections.abc import Callable

import numpy as np
from tqdm import trange

from agent.qagent import QAgent
from environment.qlearning.exploration_env import ExplorationEnv
from environment.qlearning.obstacle_avoidance_env import ObstacleAvoidanceEnv
from environment.qlearning.phototaxis_env import PhototaxisEnv


def evaluate(
    env: PhototaxisEnv | ObstacleAvoidanceEnv | ExplorationEnv,
    agents: dict[str, QAgent],
    configs: str,
    max_steps: int,
    did_succeed: Callable[[float, bool, bool], bool],
) -> tuple[dict[str, float], dict[str, float]]:
    successes = dict.fromkeys(agents.keys(), 0)
    steps_to_success = dict.fromkeys(agents.keys(), [])
    for config_idx in trange(len(configs), desc="Evaluation", unit="configuration run"):
        env.init(configs[config_idx])
        obs, _ = env.reset()
        done = False
        step = 0
        total_rewards = dict.fromkeys(agents.keys(), 0)
        while not done and step < max_steps:
            actions = {
                k: agents[k].choose_action(v, epsilon_greedy=False)
                for k, v in obs.items()
            }

            next_obs, rewards, terminateds, truncateds, _ = env.step(actions)

            dones = {
                agent_id: terminateds[agent_id] or truncateds[agent_id]
                for agent_id in agents.keys()
            }
            step += 1
            for k in agents.keys():
                total_rewards[k] += rewards[k]
                if dones[k] or step == max_steps:
                    if did_succeed(
                        rewards[k],
                        terminateds[k],
                        truncateds[k] or step == max_steps,
                    ):
                        successes[k] += 1
                        steps_to_success[k].append(step)
            done = all(dones.values())
            obs = next_obs
    success_rate = {agent_id: v / len(configs) for agent_id, v in successes.items()}
    median_steps_to_success = {
        agent_id: np.median(np.array(v)) for agent_id, v in steps_to_success.items()
    }

    return (success_rate, median_steps_to_success)
