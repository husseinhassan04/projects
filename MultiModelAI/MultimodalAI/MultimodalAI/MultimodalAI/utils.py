import logging
import os
from logging.handlers import RotatingFileHandler

def setup_logging(log_file="app.log", level=logging.INFO, max_bytes=5 * 1024 * 1024, backup_count=3):
    """
    Setup logging configuration with file rotation and console output.

    Args:
        log_file: Path to the log file. Defaults to "app.log".
        level: Logging level (e.g., logging.INFO, logging.DEBUG). Defaults to logging.INFO.
        max_bytes: Maximum size of the log file in bytes before it rotates. Defaults to 5 MB.
        backup_count: Number of backup log files to keep. Defaults to 3.
    """
    # Ensure the log directory exists
    log_dir = os.path.dirname(log_file)
    if log_dir and not os.path.exists(log_dir):
        os.makedirs(log_dir)

    # Create a rotating file handler
    file_handler = RotatingFileHandler(log_file, maxBytes=max_bytes, backupCount=backup_count)
    file_handler.setLevel(level)
    file_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))

    # Create a console handler for output to stdout
    console_handler = logging.StreamHandler()
    console_handler.setLevel(level)
    console_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))

    # Get the root logger and configure it
    logger = logging.getLogger()
    logger.setLevel(level)
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

    logging.info("Logging setup complete. Writing logs to %s", log_file)
