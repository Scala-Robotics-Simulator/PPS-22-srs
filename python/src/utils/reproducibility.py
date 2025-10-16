import numpy as np
import random

def set_global_seed(env=None, seed=None):
    """
    Set a global seed for reproducibility.

    Parameters
    ----------
    env : gym.Env or None
        Environment to seed. If None, only numpy and random are seeded.
    seed : int or None
        The seed value.
    """
    if seed is None:
        return

    np.random.seed(seed)
    random.seed(seed)

    if env is not None:
        env.reset(seed=seed)
        if hasattr(env.action_space, "seed"):
            env.action_space.seed(seed)
        if hasattr(env.observation_space, "seed"):
            env.observation_space.seed(seed)
