package io.github.srs.model.entity

/**
 * ShapeType represents different geometric shapes used in the simulation.
 */
enum ShapeType:

  /**
   * A circular shape.
   *
   * @param radius
   *   the radius of the circle must be a positive value.
   */
  case Circle(radius: Double)

  /**
   * A rectangular shape.
   *
   * @param width
   *   the width of the rectangle must be a positive value.
   * @param height
   *   the height of the rectangle must be a positive value.
   */
  case Rectangle(width: Double, height: Double)
