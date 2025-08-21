package io.github.srs.model.logic

import io.github.srs.model.ModelModule.State
import io.github.srs.model.SimulationState
import io.github.srs.model.logic.IncreaseLogic.given
import io.github.srs.model.logic.StatusLogic.given
import io.github.srs.model.logic.TimeLogic.given
import io.github.srs.model.logic.RandomLogic.given
import io.github.srs.model.logic.RobotActionsLogic.given

trait LogicsBundle[S <: State]:
  def increment: IncrementLogic[S]
  def tick: TickLogic[S]
  def random: RandomLogic[S]
  def pause: PauseLogic[S]
  def resume: ResumeLogic[S]
  def stop: StopLogic[S]
  def robotActions: RobotActionsLogic[S]

given simulationStateLogicsBundle: LogicsBundle[SimulationState] with
  val increment: IncrementLogic[SimulationState] = summon[IncrementLogic[SimulationState]]
  val tick: TickLogic[SimulationState] = summon[TickLogic[SimulationState]]
  val random: RandomLogic[SimulationState] = summon[RandomLogic[SimulationState]]
  val pause: PauseLogic[SimulationState] = summon[PauseLogic[SimulationState]]
  val resume: ResumeLogic[SimulationState] = summon[ResumeLogic[SimulationState]]
  val stop: StopLogic[SimulationState] = summon[StopLogic[SimulationState]]
  val robotActions: RobotActionsLogic[SimulationState] = summon[RobotActionsLogic[SimulationState]]
