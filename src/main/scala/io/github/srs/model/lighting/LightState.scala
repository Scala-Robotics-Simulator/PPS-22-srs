package io.github.srs.model.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.environment.EnvironmentView
import io.github.srs.model.Cell

/**
 * Represents the state of light in a two-dimensional grid.
 *
 * This class encapsulates the layout and intensity distribution of light in a discrete grid, allowing for querying
 * light intensity at specific positions and rendering the state into an ASCII or textual format.
 *
 * @param width
 *   The width of the grid (number of columns).
 * @param data
 *   The flat array storing light intensity values (in Lux) for each cell in the grid.
 */
final case class LightState private (width: Int, data: ArraySeq[Lux]):

  /**
   * The height of the grid, calculated based on the width and the length of the data array.
   */
  inline def height: Int = if width == 0 then 0 else data.length / width

  /**
   * Retrieves the light intensity at a specific cell in the grid.
   *
   * @param c
   *   The cell position to query.
   * @return
   *   The light intensity at the specified cell, or 0.0 if the cell is out of bounds.
   */
  inline def intensity(c: Cell): Lux =
    if c.x >= 0 && c.y >= 0 && c.x < width && c.y < height
    then data.lift(c.y * width + c.x).getOrElse(0.0)
    else 0.0

  /**
   * Renders the light state into a string representation.
   *
   * @param view
   *   The environment view containing obstacles and other relevant information.
   * @param ascii
   *   If true, uses ASCII characters to represent light intensity; otherwise, uses raw float values.
   * @return
   *   A string representation of the light state, formatted as rows of characters or numbers.
   */
  def render(view: EnvironmentView, ascii: Boolean): String =
    import io.github.srs.model.entity.Point2D.toCell

    val max = data.maxOption.getOrElse(0.0).max(1e-9)
    val lightSet = view.lights.iterator.map(_.position.toCell).toSet
    val robotSet = view.robots.map(_.position.toCell).toSet
    val obstacleS = view.obstacles
    (0 until height).iterator.map { y =>
      (0 until width).iterator.map { x =>
        val c = Cell(x, y)
        val raw = intensity(c) / max
        if obstacleS(c) then "#"
        else if lightSet(c) then "L"
        else if robotSet(c) then "R"
        else if ascii then Shade.char(raw).toString
        else java.lang.String.format(java.util.Locale.US, "%.2f", raw)
      }
        .mkString(if ascii then "" else " ")
    }
      .mkString("\n")
  end render

end LightState

/**
 * Companion object for the `LightState` class, providing factory methods to create or initialize instances of
 * `LightState`.
 */
object LightState:

  def empty(width: Int, h: Int): LightState =
    LightState(width, ArraySeq.fill(width * h)(0.0))

  def fromArray(width: Int, data: ArraySeq[Lux]): LightState =
    LightState(width, data)
