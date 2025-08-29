package io.github.srs.view

import cats.effect.IO
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment

/**
 * Defines how the configuration view should behave.
 */
trait ConfigurationView:

  /**
   * Initializes the configuration view and displays it to the user.
   *
   * @return
   *   an IO effect that, when run, will return the simulation configuration chosen by the user.
   */
  def init(): IO[SimulationConfig[ValidEnvironment]]

  /**
   * Closes the configuration view.
   *
   * @return
   *   an IO effect that, when run, will close the view.
   */
  def close(): IO[Unit]
