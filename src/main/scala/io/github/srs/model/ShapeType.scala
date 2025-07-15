package io.github.srs.model

/**
 * ShapeType represents different geometric shapes used in the simulation.
 */
enum ShapeType:
  /**
   * Circle shape with a specified radius.
   */
  case Circle(radius: Double)

  /**
   * Rectangle shape with specified width and height.
   */
  case Rectangle(width: Double, height: Double)
