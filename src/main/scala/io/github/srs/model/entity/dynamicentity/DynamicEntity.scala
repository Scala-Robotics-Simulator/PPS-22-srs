package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.Entity

trait DynamicEntity extends Entity:
  def actuators: Seq[Actuator[? <: DynamicEntity]]
