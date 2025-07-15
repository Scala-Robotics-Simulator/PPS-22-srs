package io.github.srs.model.entity.dynamic

type Wheels = (Double, Double)

enum Actuator:
  case WheelMotor(wheels: Wheels)
