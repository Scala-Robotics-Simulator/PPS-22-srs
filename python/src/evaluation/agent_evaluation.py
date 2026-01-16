from collections.abc import Callable

import numpy as np
from tqdm import trange

from agent.qagent import QAgent
from agent.scala_dqagent import DQAgent
from environment.qlearning.exploration_env import ExplorationEnv
from environment.qlearning.obstacle_avoidance_env import ObstacleAvoidanceEnv
from environment.qlearning.phototaxis_env import PhototaxisEnv


def evaluate(
    env: PhototaxisEnv | ObstacleAvoidanceEnv | ExplorationEnv,
    agents: dict[str, QAgent | DQAgent],
    configs: str,
    max_steps: int,
    did_succeed: Callable[[float, bool, bool, dict], bool],
    window_size: int = 100,
):
    successes = dict.fromkeys(agents.keys(), 0)
    successes_idx = {k: [] for k in agents.keys()}
    steps_to_success = {k: [] for k in agents.keys()}
    td_losses = {k: [] for k in agents.keys()}
    total_rewards = {k: [] for k in agents.keys()}
    moving_avg_reward = {k: [] for k in agents.keys()}

    for config_idx in trange(len(configs), desc="Evaluation", unit="configuration run"):
        env.init(configs[config_idx])
        obs, _ = env.reset()
        done = False
        step = 0
        episode_rewards = {k: [] for k in agents.keys()}
        episode_total_reward = dict.fromkeys(agents.keys(), 0)
        episode_td_losses = {k: [] for k in agents.keys()}
        episode_moving_avg_reward = {k: [] for k in agents.keys()}
        prev_dones = dict.fromkeys(agents.keys(), False)

        while not done and step < max_steps:
            actions = {
                k: agents[k].choose_action(v, epsilon_greedy=False)
                for k, v in obs.items()
                if not prev_dones[k]
            }

            next_obs, rewards, terminateds, truncateds, infos = env.step(actions)

            # TD-Loss per DQAgent
            for agent_id, agent in agents.items():
                if not prev_dones[agent_id] and isinstance(agent, DQAgent):
                    state = obs[agent_id]
                    action = actions[agent_id]
                    reward = rewards[agent_id]
                    next_state = next_obs[agent_id]
                    done_flag = terminateds[agent_id] or truncateds[agent_id]

                    td_loss = agent.compute_td_loss(
                        state, action, reward, next_state, done_flag
                    )
                    episode_td_losses[agent_id].append(td_loss)

            dones = {
                agent_id: terminateds[agent_id] or truncateds[agent_id]
                for agent_id in terminateds.keys()
            }

            step += 1

            for agent_id in agents.keys():
                if not prev_dones[agent_id]:
                    episode_total_reward[agent_id] += rewards[agent_id]
                    episode_rewards[agent_id].append(rewards[agent_id])

                    if len(episode_rewards[agent_id]) == 0:
                        moving_avg = 0.0
                    elif len(episode_rewards[agent_id]) < window_size:
                        moving_avg = episode_rewards[agent_id][-1]
                    else:
                        moving_avg = np.mean(episode_rewards[agent_id][-window_size:])

                    episode_moving_avg_reward[agent_id].append(moving_avg)

                    if did_succeed(
                        rewards[agent_id],
                        terminateds[agent_id],
                        truncateds[agent_id] or step == max_steps,
                        infos.get(agent_id, {}),
                    ):
                        dones[agent_id] = True
                        successes[agent_id] += 1
                        successes_idx[agent_id].append(config_idx)
                        steps_to_success[agent_id].append(step)

            done = all(dones.values())
            obs = next_obs
            prev_dones = dones

        for agent_id in agents.keys():
            td_losses[agent_id].append(episode_td_losses[agent_id])
            total_rewards[agent_id].append(episode_total_reward[agent_id])
            moving_avg_reward[agent_id].append(episode_moving_avg_reward[agent_id])

    success_rate = {agent_id: v / len(configs) for agent_id, v in successes.items()}
    median_steps_to_success = {
        agent_id: np.median(np.array(v)) for agent_id, v in steps_to_success.items()
    }

    return {
        "success_rate": success_rate,
        "successes_idx": successes_idx,
        "steps_to_success": steps_to_success,
        "median_steps_to_success": median_steps_to_success,
        "total_rewards": total_rewards,
        "moving_avg_reward": moving_avg_reward,
        "td_losses": td_losses,
    }
