package io.github.srs.model.illumination.model

import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

/**
 * Represents a rectangular 2D grid addressed as grid(x)(y).
 * @param A
 *   The type of elements stored in the grid.
 */
type Grid[A] = Array[Array[A]]

/**
 * Companion object for the [[Grid]] type, providing utility methods for grid manipulation.
 */
object Grid:

  /**
   * Create a grid by tabulating a function over coordinates.
   *
   * @param w
   *   The width of the grid (number of columns).
   * @param h
   *   The height of the grid (number of rows).
   * @param f
   *   A function that takes the x and y coordinates and returns the value for that cell.
   * @tparam A
   *   The type of elements in the grid.
   * @return
   *   A two-dimensional array representing the grid.
   */
  def tabulate[A: ClassTag](w: Int, h: Int)(f: (Int, Int) => A): Array[Array[A]] =
    Array.tabulate(w)(x => Array.tabulate(h)(y => f(x, y)))

  /**
   * Overlay two grids taking the maximum at each cell.
   *
   * @param base
   *   The base grid.
   * @param over
   *   The overlay grid.
   * @return
   *   A new grid where each cell contains the maximum value from the corresponding cells of the base and overlay grids.
   */
  def overlayMax[A: ClassTag](base: Grid[A], over: Grid[A])(using num: Numeric[A]): Grid[A] =
    overlayWith(base, over)(num.zero)(num.max)

  /**
   * Overlay with a custom zero value and combiner.
   *
   * @param base
   *   The base grid.
   * @param over
   *   The overlay grid.
   * @param zero
   *   The default value to use for cells outside the bounds of the overlay grid.
   * @param combine
   *   A function that combines two cell values.
   * @tparam A
   *   The type of elements in the grid.
   * @return
   *   A new grid where each cell is the result of applying the combination function to the corresponding cells of the
   *   base and overlay grids.
   */
  private def overlayWith[A: ClassTag](base: Grid[A], over: Grid[A])(zero: A)(combine: (A, A) => A): Grid[A] =
    if base.isEmpty then base
    else if over.isEmpty then base
    else
      val bw = base.length
      val bh = base(0).length
      tabulate(bw, bh) { (x, y) =>
        val b = base(x)(y)
        val o =
          if x < over.length && y < over(x).length then over(x)(y)
          else zero
        combine(b, o)
      }

  /**
   * Extension methods for grids of any type.
   */
  extension [A](g: Array[Array[A]])

    /**
     * Gets the width (cells) of the grid (number of columns).
     *
     * @return
     *   The width of the grid.
     */
    inline def width: Int =
      g.length

    /**
     * Gets the height (cells) of the grid (number of rows).
     *
     * @return
     *   The height of the grid.
     */
    inline def height: Int =
      g.headOption.map(_.length).getOrElse(0)

    /**
     * Checks if the grid is empty.
     * @return
     *   `true` if the grid is empty, `false` otherwise.
     */
    inline def isEmpty: Boolean =
      g.headOption.forall(_.isEmpty)

    /**
     * Checks if the given coordinates are within the bounds of the grid.
     *
     * @param x
     *   The x-coordinate to check.
     * @param y
     *   The y-coordinate to check.
     * @return
     *   `true` if the coordinates are within bounds, `false` otherwise.
     */
    inline def inBounds(x: Int, y: Int): Boolean =
      x >= 0 && y >= 0 && x < width && y < height

  end extension

  /**
   * Extension methods for grids of type `Array[Array[A]]` where `A` is a `ClassTag`.
   */
  extension [A: ClassTag](g: Array[Array[A]])

    /**
     * Flattens the grid in row-major order (x-fast).
     *
     * @return
     *   An [[ArraySeq]] containing all elements of the grid in row-major order.
     */
    def flattenRowMajor: ArraySeq[A] =
      ArraySeq.unsafeWrapArray(g.transpose.flatten)

end Grid
