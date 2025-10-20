import logging
import sys


class Logger:
    """Simple logger class for logging messages.

    Parameters
    ----------
    name : str
        Name of the class to be used in the logger.
    """
    def __init__(self, name, level=logging.INFO):
        self.logger = logging.getLogger(name)
        if not self.logger.handlers:
            handler = logging.StreamHandler(sys.stdout)
            formatter = logging.Formatter("%(asctime)s — %(levelname)s — %(message)s")
            handler.setFormatter(formatter)
            self.logger.addHandler(handler)
            self.logger.setLevel(level)
            self.logger.propagate = False

    def info(self, message):
        """Logs an info message."""
        self.logger.info(message)

    def warning(self, message):
        """Logs a warning message."""
        self.logger.warning(message)

    def error(self, message):
        """Logs an error message."""
        self.logger.error(message)
