package io.github.srs.model.logic

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.{ ModelModule, SimulationState }
import cats.effect.IO

/**
 * Logic for handling simulation time updates.
 * @tparam S
 *   the type of the simulation state.
 */
trait TickLogic[S <: ModelModule.State]:
  /**
   * Updates the simulation state by advancing the elapsed time by the given delta duration.
   * @param s
   *   the current simulation state.
   * @param delta
   *   the duration to advance the elapsed time.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with the new elapsed time.
   */
  def tick(s: S, delta: FiniteDuration): IO[S]

  /**
   * Updates the simulation state by changing the simulation speed.
   * @param s
   *   the current simulation state.
   * @param speed
   *   the new simulation speed to set.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with the new simulation speed.
   */
  def tickSpeed(s: S, speed: SimulationSpeed): IO[S]

/**
 * Companion object for [[TickLogic]] containing given instances.
 */
object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): IO[SimulationState] =
      val updatedElapsed = s.elapsedTime + delta
      IO.pure(s.copy(elapsedTime = updatedElapsed))

    def tickSpeed(s: SimulationState, speed: SimulationSpeed): IO[SimulationState] =
      IO.pure(s.copy(simulationSpeed = speed))
