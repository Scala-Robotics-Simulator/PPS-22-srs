package io.github.srs.model.lighting

/**
 * Utility object for converting light intensity values to ASCII art characters. Provides visual representation of
 * different light intensity levels using Unicode block elements.
 */
object Shade:

  /**
   * Maps a light intensity value to a corresponding ASCII character.
   *
   * The mapping follows these thresholds:
   *   - >= 0.80: Full block (█)
   *   - >= 0.60: Dark shade (▓)
   *   - >= 0.30: Medium shade (▒)
   *   - >= 0.05: Light shade (░)
   *   - < 0.05: Dot (.)
   *
   * @param intensity
   *   The light intensity value between 0.0 and 1.0
   * @return
   *   A character representing the light intensity level
   */
  def char(intensity: Lux): Char =
    if intensity >= 0.80 then '█'
    else if intensity >= 0.60 then '▓'
    else if intensity >= 0.30 then '▒'
    else if intensity >= 0.05 then '░'
    else '.'
end Shade
