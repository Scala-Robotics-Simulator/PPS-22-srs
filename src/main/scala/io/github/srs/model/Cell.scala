package io.github.srs.model

/**
 * A single square in a grid, defined by its x and y coordinates.
 *
 * Cells represent positions in a grid-based two-dimensional map.
 *
 * The coordinates are zero-indexed, meaning that the top-left cell is at (0, 0).
 *
 * @param x
 *   the coordinate of the cell (horizontal position).
 * @param y
 *   the coordinate of the cell (vertical position).
 */
final case class Cell(x: Int, y: Int)
