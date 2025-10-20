import logging
import os
import shutil
import subprocess
import uuid

import cv2
import numpy as np

logger = logging.getLogger(__name__)


def create_mp4_video_from_frames(frames: np.ndarray, fps: int) -> str:
    """
    Create a temporary MP4 video from a list of frames and return its file path.
    """
    temp_video_path = "tempfile.mp4"
    compressed_path = f"{uuid.uuid4()}.mp4"

    height, width = frames[0].shape[:2]
    out = cv2.VideoWriter(
        temp_video_path, cv2.VideoWriter_fourcc(*"mp4v"), fps, (width, height)
    )

    for frame in frames:
        out.write(frame[..., ::-1].copy())  # RGB -> BGR
    out.release()

    ffmpeg_path = shutil.which("ffmpeg")
    if ffmpeg_path is None:
        raise FileNotFoundError("ffmpeg not found in PATH")

    try:
        subprocess.run(
            [
                ffmpeg_path,
                "-y",
                "-i",
                temp_video_path,
                "-vcodec",
                "libx264",
                compressed_path,
            ],
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.STDOUT,
        )
        logger.info(f"Compressed video saved to {compressed_path}")
    except subprocess.CalledProcessError as e:
        logger.error(f"Video compression failed: {e}")
        raise
    finally:
        if os.path.exists(temp_video_path):
            os.remove(temp_video_path)

    return compressed_path
