import logging
from pathlib import Path

logging.basicConfig(level=logging.INFO, format="%(message)s")
logger = logging.getLogger(__name__)


def get_yaml_path(*subpaths):
    """
    Builds the full path to a YAML file located in the project directory.

    Args:
        *subpaths: subpaths to append to the project root.

    Returns:
        Path: The full path to the YAML file.
    """
    project_root = Path(__file__).resolve().parents[3]
    return project_root.joinpath(*subpaths)


def read_file(path: str) -> str:
    """
    Reads the contents of a file and returns it as a string.

    Args:
        path: The path of the file to read.

    Returns:
        str: The contents of the file as a string.
    """
    try:
        with open(path, encoding="utf-8") as file:
            return file.read()
    except FileNotFoundError:
        logger.error(f"Error: the file '{path}' was not found.")
        return ""
    except Exception as e:
        logger.error(f"Error while reading the file: {e}")
        return ""
