import cv2
import os
import subprocess
import uuid

import numpy as np


def create_mp4_video_from_frames(frames: np.ndarray, fps: int) -> str:
    """
    Create a temporary MP4 video from a list of frames and return its file path.

    Parameters
    ----------
    frames : np.ndarray
        List of video frames in RGB format.
    fps : int
        Frames per second for the video.

    Returns
    -------
    str
        Path to the created MP4 video file.
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

    subprocess.run(
        ["ffmpeg", "-y", "-i", temp_video_path, "-vcodec", "libx264", compressed_path],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )

    os.remove(temp_video_path)
    return compressed_path
