package io.github.srs.controller

import scala.concurrent.duration.FiniteDuration

enum Event derives CanEqual:
  case Increment
  case Stop
  case Tick(delta: FiniteDuration)
