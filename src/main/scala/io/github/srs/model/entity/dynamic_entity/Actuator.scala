package io.github.srs.model.entity.dynamic_entity

trait Actuator[E <: DynamicEntity]:
  def act(entity: E): E
