package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

object SimulationConfig:

  enum SimulationStatus derives CanEqual:
    case RUNNING
    case PAUSED
    case STOPPED
    case ELAPSED_TIME

  enum SimulationSpeed derives CanEqual:
    case SLOW
    case NORMAL
    case FAST
    case SUPERFAST

    def tickSpeed: FiniteDuration = this match
      case SLOW => FiniteDuration(200, MILLISECONDS)
      case NORMAL => FiniteDuration(100, MILLISECONDS)
      case FAST => FiniteDuration(10, MILLISECONDS)
      case SUPERFAST => FiniteDuration(0, MILLISECONDS)
