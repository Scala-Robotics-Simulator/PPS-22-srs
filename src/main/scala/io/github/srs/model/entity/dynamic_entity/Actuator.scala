package io.github.srs.model.entity.dynamic_entity

trait Actuator:
  def act(robot: Robot): Robot
