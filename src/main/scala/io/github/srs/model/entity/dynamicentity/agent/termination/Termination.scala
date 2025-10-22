package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * An enumeration of available termination models for agents.
 *
 * This enum represents the different types of termination models that can be applied to agents in the simulation. Each
 * case corresponds to a specific termination evaluation strategy.
 */
enum Termination(val name: String) derives CanEqual:
  case NeverTerminate extends Termination("NeverTerminate")

  /**
   * Converts the enum case to its corresponding [[TerminationModel]] instance.
   *
   * @return
   *   the [[TerminationModel]] implementation for this termination type
   */
  def toTerminationModel: TerminationModel[Agent] =
    this match
      case NeverTerminate => io.github.srs.model.entity.dynamicentity.agent.termination.NeverTerminate()

  /**
   * String representation of the termination type.
   * @return
   *   name of the termination
   */
  override def toString: String = name
