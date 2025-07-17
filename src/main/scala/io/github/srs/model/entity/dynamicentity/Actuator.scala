package io.github.srs.model.entity.dynamicentity

trait Actuator[E <: DynamicEntity]:
  def act(entity: E): E
