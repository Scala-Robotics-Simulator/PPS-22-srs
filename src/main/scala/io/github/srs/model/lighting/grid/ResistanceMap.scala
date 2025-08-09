package io.github.srs.model.lighting.grid

/**
 * Represents a resistance map for a [[SubGridSize]], storing resistance values for each cell.
 *
 * @param size
 *   The size of the [[SubGridSize]].
 * @param data
 *   A 2D array containing the resistance values for each cell.
 */
final case class ResistanceMap private (size: SubGridSize, private val data: Array[Array[Double]]):

  /**
   * @return
   *   The width of the grid.
   */
  inline def width: Int = size.width

  /**
   * @return
   *   The height of the grid.
   */
  inline def height: Int = size.height

  /**
   * Checks if the specified [[SubCell]] is within the grid bounds.
   *
   * @param c
   *   The [[SubCell]] containing the coordinates.
   * @return
   *   True if the [[SubCell]] is within bounds, false otherwise.
   */
  inline def contains(c: SubCell): Boolean =
    c.x >= 0 && c.x < width && c.y >= 0 && c.y < height

  /**
   * Retrieves the resistance value at the specified sub-cell.
   *
   * @param c
   *   The [[SubCell]] containing the coordinates.
   * @return
   *   The resistance value at the specified sub-cell.
   */
  inline def valueAt(c: SubCell): Double = data(c.x)(c.y)

  /**
   * Retrieves the resistance value at the specified [[SubCell]], if within bounds.
   *
   * @param c
   *   The [[SubCell]] containing the coordinates.
   * @return
   *   An [[Option]] containing the resistance value if within bounds, or [[None]] otherwise.
   */
  def get(c: SubCell): Option[Double] = if contains(c) then Some(data(c.x)(c.y)) else None

  /**
   * Creates a new [[ResistanceMap]] with the specified value set at the given [[SubCell]].
   *
   * @param c
   *   The [[SubCell]] containing the coordinates.
   * @param v
   *   The new resistance value to set.
   * @return
   *   A new [[ResistanceMap]] with the updated value.
   */
  def setAt(c: SubCell, v: Double): ResistanceMap =
    val copy = ResistanceMap.clone2D(data)
    copy(c.x)(c.y) = v
    ResistanceMap(size, copy)

  /**
   * Creates a new [[ResistanceMap]] with the value at the specified [[SubCell]] updated by applying the given function.
   * @param c
   *   The [[SubCell]] containing the coordinates.
   * @param f
   *   The function to apply to the current resistance value at the specified sub-cell.
   * @return
   *   A new [[ResistanceMap]] with the updated value.
   */
  def updatedAt(c: SubCell)(f: Double => Double): ResistanceMap =
    val copy = ResistanceMap.clone2D(data)
    copy(c.x)(c.y) = f(copy(c.x)(c.y))
    ResistanceMap(size, copy)

  /**
   * Creates a defensive copy of the internal data as a mutable 2D array.
   *
   * @return
   *   A mutable 2D array containing the resistance values.
   */
  def toMutableArray2D: Array[Array[Double]] = ResistanceMap.clone2D(data)

end ResistanceMap

object ResistanceMap:

  /**
   * Creates a [[ResistanceMap]] filled with zeros.
   *
   * @param size
   *   The size of the grid.
   * @return
   *   A new [[ResistanceMap]] with all cells initialized to 0.0.
   */
  def zeros(size: SubGridSize): ResistanceMap =
    ResistanceMap(size, Array.fill(size.width, size.height)(0.0))

  /**
   * Creates a [[ResistanceMap]] filled with a specified value.
   *
   * @param size
   *   The size of the grid.
   * @param value
   *   The value to fill all cells with.
   * @return
   *   A new [[ResistanceMap]] with all cells initialized to the specified value.
   */
  def filled(size: SubGridSize, value: Double): ResistanceMap =
    ResistanceMap(size, Array.fill(size.width, size.height)(value))

  /**
   * Builds a new [[ResistanceMap]] with a custom initialization function.
   * @param size
   *   The size of the grid.
   * @param fill
   *   The initial value to fill the buffer with (default is 0.0).
   * @param init
   *   A function to initialize the buffer.
   * @return
   *   A new `ResistanceMap` with the initialized values.
   */
  def build(size: SubGridSize, fill: Double = 0.0)(init: Array[Array[Double]] => Unit): ResistanceMap =
    val buf = Array.fill(size.width, size.height)(fill)
    init(buf)
    ResistanceMap(size, buf)

  /**
   * Copies rows from the source 2D array to the destination 2D array.
   *
   * @param src
   *   The source 2D array.
   * @param out
   *   The destination 2D array.
   * @param i
   *   The current row index (default is 0).
   * @return
   *   The destination 2D array with copied rows.
   */
  @annotation.tailrec
  private def copyRows(src: Array[Array[Double]], out: Array[Array[Double]], i: Int = 0): Array[Array[Double]] =
    if i < src.length then
      out(i) = src(i).clone()
      copyRows(src, out, i + 1)
    else out

  /**
   * Creates a deep copy of a 2D array.
   *
   * @param src
   *   The source 2D array to copy.
   * @return
   *   A new 2D array that is a deep copy of the source.
   */
  private[grid] def clone2D(src: Array[Array[Double]]): Array[Array[Double]] =
    val out = new Array[Array[Double]](src.length)
    copyRows(src, out)
end ResistanceMap
