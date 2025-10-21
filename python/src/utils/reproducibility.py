import logging
import os
import random

import numpy as np

logger = logging.getLogger(__name__)


def set_global_seed(seed=None, env=None, tensorflow_deterministic=False, verbose=False):
    """
    Set a global seed for reproducibility across libraries and environments.

    Parameters
    ----------
    seed : int or None
        The seed value. If None, does nothing.
    env : gym.Env or None
        Optional Gymnasium or Gym environment to seed.
    tensorflow_deterministic : bool
        If True, sets TensorFlow to operate in a deterministic mode.
    verbose: bool
        If True, logging.infos out information about the seeding process.
    """
    if seed is None:
        if verbose:
            logging.info("[set_global_seed] No seed provided, skipping.")
        return

    os.environ["PYTHONHASHSEED"] = str(seed)
    random.seed(seed)
    np.random.seed(seed)

    if tensorflow_deterministic:
        _seed_tensorflow(seed=seed, verbose=verbose)

    _seed_gymnasium(seed=seed, env=env, verbose=verbose)

    if verbose:
        logging.info(f"[set_global_seed] All available seeds set to {seed}.")


def _seed_tensorflow(seed, verbose=False):
    """Set TensorFlow seed for reproducibility.

    Parameters
    ----------
    seed : int
        The seed value.
    verbose: bool
        If True, logging.infos out information about the seeding process.
    """
    try:
        import tensorflow as tf
    except ImportError:
        if verbose:
            logging.warning("[set_global_seed] TensorFlow not installed, skipping.")
        return

    tf.random.set_seed(seed)
    try:
        tf.config.experimental.enable_op_determinism(True)
    except Exception as e:
        logger.warning(
            f"[set_global_seed] Could not enable TensorFlow op determinism: {e}"
        )

    if verbose:
        logging.info("[set_global_seed] TensorFlow seed set.")


def _seed_gymnasium(seed, env, verbose=False):
    """Set Gymnasium environment seed for reproducibility.

    Parameters
    ----------
    seed : int
        The seed value.
    env : gym.Env or None
        Optional Gymnasium or Gym environment to seed.
    verbose: bool
        If True, logging.infos out information about the seeding process.
    """
    if env is None:
        return
    try:
        env.reset(seed=seed)
        if hasattr(env.action_space, "seed"):
            env.action_space.seed(seed)
        if hasattr(env.observation_space, "seed"):
            env.observation_space.seed(seed)
        if verbose:
            logging.info("[set_global_seed] Gymnasium environment seed set.")
    except Exception as e:
        if verbose:
            logging.warning(
                f"[set_global_seed] Warning: could not seed environment ({e})"
            )
