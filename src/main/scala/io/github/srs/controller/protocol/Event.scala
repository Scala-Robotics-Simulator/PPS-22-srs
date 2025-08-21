package io.github.srs.controller.protocol

import scala.concurrent.duration.FiniteDuration

import io.github.srs.controller.message.RobotProposal
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.utils.random.RNG

/**
 * Protocol defining the events that can occur in the simulation.
 */
enum Event:
  /**
   * Event indicating the simulation has stopped.
   */
  case Stop

  /**
   * Event indicating the simulation is paused.
   */
  case Pause

  /**
   * Event indicating the simulation has resumed.
   */
  case Resume

  /**
   * Event representing a tick in the simulation with a specified time delta.
   * @param delta
   *   the time duration for the tick.
   */
  case Tick(delta: FiniteDuration)

  /**
   * Event representing a change in the simulation speed.
   * @param speed
   *   the new [[SimulationSpeed]].
   */
  case TickSpeed(speed: SimulationSpeed)

  /**
   * Event representing a random event in the simulation.
   * @param rng
   *   the random number generator used for the event.
   */
  case Random(rng: RNG)

  /**
   * Event representing a proposal for robot actions.
   * @param proposals
   *   the list of robot proposals to be processed.
   */
  case RobotActionProposals(proposals: List[RobotProposal])
end Event
