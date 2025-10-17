package io.github.srs.model.logic

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.model.BaseSimulationState

trait BaseTickLogic[S <: ModelModule.BaseState]:
  /**
   * Updates the simulation state by advancing the elapsed time by the given delta duration.
   * @param s
   *   the current simulation state.
   * @param delta
   *   the duration to advance the elapsed time.
   * @return
   *   an [[cats.effect.IO]] effect that produces the updated simulation state with the new elapsed time.
   */
  def tick(s: S, delta: FiniteDuration): IO[S]

/**
 * Logic for handling simulation time updates.
 * @tparam S
 *   the type of the simulation state.
 */
trait TickLogic[S <: ModelModule.BaseState] extends BaseTickLogic[S]:

  /**
   * Updates the simulation state by changing the simulation speed.
   * @param s
   *   the current simulation state.
   * @param speed
   *   the new simulation speed to set.
   * @return
   *   an [[cats.effect.IO]] effect that produces the updated simulation state with the new simulation speed.
   */
  def tickSpeed(s: S, speed: SimulationSpeed): IO[S]

/**
 * Companion object for [[TickLogic]] containing given instances.
 */
object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): IO[SimulationState] = IO.pure:
      val updatedElapsed = s.elapsedTime + delta

      val isElapsedTimeReached: Boolean = s.simulationTime.exists(max => s.elapsedTime >= max)

      s.copy(
        elapsedTime = updatedElapsed,
        simulationStatus = if isElapsedTimeReached then SimulationStatus.ELAPSED_TIME else s.simulationStatus,
      )

    def tickSpeed(s: SimulationState, speed: SimulationSpeed): IO[SimulationState] = IO.pure:
      s.copy(simulationSpeed = speed)

  given BaseTickLogic[BaseSimulationState] with

    def tick(s: BaseSimulationState, delta: FiniteDuration): IO[BaseSimulationState] = IO.pure:
      val updatedElapsed = s.elapsedTime + delta

      s.copy(
        elapsedTime = updatedElapsed,
      )
end TimeLogic
