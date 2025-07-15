package io.github.srs.model.entity.dynamic

import io.github.srs.model.entity.Entity

trait DynamicEntity extends Entity:
  def actuators: Option[Seq[Actuator]]
