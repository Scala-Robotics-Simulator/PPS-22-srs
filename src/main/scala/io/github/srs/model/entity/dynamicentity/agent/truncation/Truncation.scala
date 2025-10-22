package io.github.srs.model.entity.dynamicentity.agent.truncation

import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * An enumeration of available truncation models for agents.
 *
 * This enum represents the different types of truncation models that can be applied to agents in the simulation. Each
 * case corresponds to a specific truncation evaluation strategy.
 */
enum Truncation(val name: String) derives CanEqual:
  case NeverTruncate extends Truncation("NeverTruncate")

  /**
   * Converts the enum case to its corresponding [[TruncationModel]] instance.
   *
   * @return
   *   the [[TruncationModel]] implementation for this truncation type
   */
  def toTruncationModel: TruncationModel[Agent] =
    this match
      case NeverTruncate => io.github.srs.model.entity.dynamicentity.agent.truncation.NeverTruncate()

  /**
   * String representation of the truncation type.
   * @return
   *   name of the truncation
   */
  override def toString: String = name
