import os
import sys
from pathlib import Path
from typing import Any

import numpy as np
import pygame
import yaml

from utils.log import Logger

logger = Logger(__name__)


class EnvironmentGenerator:
    def __init__(
        self,
        env_width: float = 10.0,
        env_height: float = 10.0,
        agent_num: int = 1,
        agent_radius: float = 0.25,
        agent_speed: float = 1.0,
        min_obstacles: int = 0,
        max_obstacles: int = 5,
        min_obstacle_width: float = 1.0,
        max_obstacle_width: float = 7.0,
        min_obstacle_height: float = 0.2,
        max_obstacle_height: float = 1.0,
        min_lights: int = 0,
        max_lights: int = 5,
        light_radius: float = 0.2,
        light_illumination_radius: float = 5.0,
        light_intensity: float = 1.0,
        light_attenuation: float = 1.0,
        seed: int = 42,
        np_seed: int | None = None,
    ):
        self.env_width = env_width
        self.env_height = env_height
        self.agent_num = agent_num
        self.agent_radius = agent_radius
        self.agent_speed = agent_speed
        self.min_obstacles = min_obstacles
        self.max_obstacles = max_obstacles
        self.min_obstacle_width = min_obstacle_width
        self.max_obstacle_width = max_obstacle_width
        self.min_obstacle_height = min_obstacle_height
        self.max_obstacle_height = max_obstacle_height
        self.min_lights = min_lights
        self.max_lights = max_lights
        self.light_radius = light_radius
        self.light_illumination_radius = light_illumination_radius
        self.light_intensity = light_intensity
        self.light_attenuation = light_attenuation
        self.seed = seed if seed is not None else np.random.randint(0, 999999)
        self.rng = np.random.default_rng(np_seed)

    def generate_agent(self, agent_id: str) -> dict[str, Any]:
        """Generate agent configuration."""
        pos = self.rng.uniform([0, 0], [self.env_width, self.env_height])
        orientation = self.rng.uniform(0, 360)

        return {
            "agent": {
                "id": agent_id,
                "radius": self.agent_radius,
                "withProximitySensors": True,
                "withLightSensors": True,
                "position": [round(float(pos[0]), 1), round(float(pos[1]), 1)],
                "orientation": round(float(orientation), 1),
                "speed": self.agent_speed,
                "reward": "ExplorationDQN",
                "termination": "ExplorationTermination",
                "truncation": "NeverTruncate",
            }
        }

    def generate_lights(self, num_lights: int) -> list[dict[str, Any]]:
        """Generate multiple light configurations at once."""
        if num_lights == 0:
            return []

        positions = self.rng.uniform(
            [0, 0], [self.env_width, self.env_height], (num_lights, 2)
        )
        orientations = self.rng.uniform(0, 360, num_lights)

        lights = []
        for i in range(num_lights):
            lights.append(
                {
                    "light": {
                        "orientation": round(float(orientations[i]), 1),
                        "illuminationRadius": self.light_illumination_radius,
                        "radius": self.light_radius,
                        "intensity": self.light_intensity,
                        "attenuation": self.light_attenuation,
                        "position": [
                            round(float(positions[i, 0]), 1),
                            round(float(positions[i, 1]), 1),
                        ],
                    }
                }
            )
        return lights

    def generate_obstacles(self, num_obstacles: int) -> list[dict[str, Any]]:
        """Generate multiple obstacle configurations at once."""
        if num_obstacles == 0:
            return []

        positions = self.rng.uniform(
            [0, 0], [self.env_width, self.env_height], (num_obstacles, 2)
        )

        # Generate widths first
        widths = self.rng.uniform(
            self.min_obstacle_width, self.max_obstacle_width, num_obstacles
        )

        # Calculate heights inversely proportional to widths
        # Normalize widths to [0, 1] range
        width_range = self.max_obstacle_width - self.min_obstacle_width
        height_range = self.max_obstacle_height - self.min_obstacle_height
        normalized_widths = (widths - self.min_obstacle_width) / width_range

        # Inverse relationship: large width -> small height
        base_heights = self.min_obstacle_height + (1 - normalized_widths) * height_range

        # Add variance (e.g., ±20% of the height range)
        variance_factor = 0.2  # Adjust this to control variance (0.0 = no variance, 0.5 = high variance)
        variance = self.rng.uniform(
            -variance_factor * height_range,
            variance_factor * height_range,
            num_obstacles,
        )
        heights = np.clip(
            base_heights + variance, self.min_obstacle_height, self.max_obstacle_height
        )

        orientations = self.rng.uniform(0, 360, num_obstacles)

        obstacles = []
        for i in range(num_obstacles):
            obstacles.append(
                {
                    "obstacle": {
                        "orientation": round(float(orientations[i]), 1),
                        "position": [
                            round(float(positions[i, 0]), 1),
                            round(float(positions[i, 1]), 1),
                        ],
                        "height": round(float(heights[i]), 1),
                        "width": round(float(widths[i]), 1),
                    }
                }
            )
        return obstacles

    def generate_environment(self) -> dict[str, Any]:
        """Generate complete environment configuration."""
        entities = []

        # 1. Generate agent
        for i in range(self.agent_num):
            entities.append(
                self.generate_agent(f"00000000-0000-0000-0000-{(i + 1):012d}")
            )

        # 2. Generate lights
        num_lights = self.rng.integers(self.min_lights, self.max_lights + 1)
        entities.extend(self.generate_lights(num_lights))

        # 3. Generate obstacles
        num_obstacles = self.rng.integers(self.min_obstacles, self.max_obstacles + 1)
        entities.extend(self.generate_obstacles(num_obstacles))

        return {
            "simulation": {"seed": int(self.seed)},
            "environment": {
                "width": self.env_width,
                "height": self.env_height,
                "entities": entities,
            },
        }


def validate_yaml(env, yaml_content: str) -> bool:
    """
    Mock function to validate YAML content.
    Replace this with actual validation logic.
    """
    ok, _ = env.init(yaml_content)
    if ok:
        pygame.init()
        screen = pygame.display.set_mode((800, 600))
        pygame.display.set_caption("Save the environment? y/n")
        rgb_array = env.render()
        surface = pygame.surfarray.make_surface(np.transpose(rgb_array, (1, 0, 2)))
        screen.blit(surface, (0, 0))
        pygame.display.flip()
        while True:
            event = pygame.event.wait()
            if event.type == pygame.QUIT:
                sys.exit(0)
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_y:
                    pygame.quit()
                    return True
                if event.key == pygame.K_n:
                    pygame.quit()
                    return False
    return False


def get_next_index(output_dir: str) -> int:
    """Get the next available index for file naming."""
    path = Path(output_dir)
    if not path.exists():
        return 0

    existing_files = list(path.glob("environment_*.yml"))
    if not existing_files:
        return 0

    indices = []
    for f in existing_files:
        try:
            idx = int(f.stem.split("_")[1])
            indices.append(idx)
        except (ValueError, IndexError):
            continue

    return max(indices) + 1 if indices else 0


def save_yaml(env, content: dict[str, Any], output_dir: str, index: int) -> str:
    """
    Save YAML content to file with given index.
    Returns the path of the saved file.
    """
    # Generate YAML string with optimized settings
    yaml_str = yaml.dump(
        content,
        default_flow_style=False,
        sort_keys=False,
        allow_unicode=True,
        width=float("inf"),
    )

    # Validate before saving
    if not validate_yaml(env, yaml_str):
        raise ValueError("Generated YAML failed validation")

    # Save to file
    filename = f"environment_{index:04d}.yml"
    filepath = os.path.join(output_dir, filename)

    with open(filepath, "w") as f:
        f.write(yaml_str)

    return filepath


def generate_multiple_environments(
    env,
    num_environments: int,
    generator_config: dict[str, Any] or None = None,
    output_dir: str = "resources/generated",
    max_retries: int or None = 1000,
) -> list[str]:
    """
    Generate multiple VALID environment YAML files.
    Only counts successfully validated environments towards the total.

    Args:
        num_environments: Number of VALID environments to generate
        generator_config: Configuration for EnvironmentGenerator
        output_dir: Directory to save generated files
        max_retries: Maximum total attempts before giving up

    Returns:
        List of file paths for generated YAML files
    """
    if generator_config is None:
        generator_config = {}

    # Create directory once
    Path(output_dir).mkdir(parents=True, exist_ok=True)

    # Get starting index once
    current_index = get_next_index(output_dir)

    generated_files = []
    attempts = 0

    while len(generated_files) < num_environments and (
        max_retries is None or attempts < max_retries
    ):
        attempts += 1

        # Create new generator with random seed for each environment
        config = generator_config.copy()
        config["seed"] = np.random.randint(0, 999999)

        generator = EnvironmentGenerator(**config)
        env_data = generator.generate_environment()

        try:
            filepath = save_yaml(env, env_data, output_dir, current_index)
            generated_files.append(filepath)
            logger.info(
                f"Generated [{len(generated_files)}/{num_environments}]: {filepath}"
            )
            current_index += 1
        except ValueError:
            # Validation failed, try again without counting
            logger.debug(f"Validation failed (attempt {attempts}), retrying...")
            continue

    if len(generated_files) < num_environments:
        logger.error(
            f"\nWarning: Only generated {len(generated_files)}/{num_environments} valid environments after {attempts} attempts"
        )

    return generated_files
