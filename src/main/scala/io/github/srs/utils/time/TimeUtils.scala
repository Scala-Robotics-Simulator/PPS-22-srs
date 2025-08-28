package io.github.srs.utils.time

import scala.concurrent.duration.FiniteDuration

object TimeUtils:

  /**
   * Formats a given duration into a string representation in the format "HH:MM:SS:SSS", where HH are hours, MM are
   * minutes, SS are seconds, and SSS are milliseconds.
   *
   * @param duration
   *   the duration to format.
   * @return
   *   a string representing the duration in "HH:MM:SS:SSS" format.
   */
  def formatTime(duration: FiniteDuration): String =
    val totalMillis = duration.toMillis
    val hours = (totalMillis / 3600000) % 24
    val minutes = (totalMillis / 60000) % 60
    val seconds = (totalMillis / 1000) % 60
    val millis = totalMillis % 1000
    f"$hours%02d:$minutes%02d:$seconds%02d:$millis%03d"
