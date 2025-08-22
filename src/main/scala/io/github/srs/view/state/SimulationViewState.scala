package io.github.srs.view.state

import java.awt.image.BufferedImage

import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.environment.{ robots, Environment }

/**
 * Centralized state management for the simulation view eliminating the need for multiple state variables.
 */
final case class SimulationViewState(
    environment: Option[Environment] = None,
    selectedRobotId: Option[String] = None,
    robots: List[Robot] = Nil,
    staticLayer: Option[BufferedImage] = None,
    lastCanvasSize: (Int, Int, Int, Int) = (-1, -1, -1, -1),
):

  def withEnvironment(env: Environment): SimulationViewState =
    copy(
      environment = Some(env),
      robots = env.robots.sortBy(_.id),
    )

  def withSelection(id: Option[String]): SimulationViewState =
    copy(selectedRobotId = id)

  def withStaticLayer(img: BufferedImage, size: (Int, Int, Int, Int)): SimulationViewState =
    copy(staticLayer = Some(img), lastCanvasSize = size)

  def needsStaticLayerUpdate(newSize: (Int, Int, Int, Int)): Boolean =
    staticLayer.isEmpty || lastCanvasSize != newSize

  def selectedRobot: Option[Robot] =
    selectedRobotId.flatMap(id => robots.find(_.id.toString == id))
end SimulationViewState
