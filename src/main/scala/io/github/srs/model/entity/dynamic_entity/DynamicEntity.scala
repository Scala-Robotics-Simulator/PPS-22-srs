package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.Entity

trait DynamicEntity extends Entity:
  def actuators: Seq[Actuator]
