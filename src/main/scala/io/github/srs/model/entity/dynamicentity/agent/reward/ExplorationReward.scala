package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.exp

import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_T_T
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CoverageTermination.*
import io.github.srs.utils.SpatialUtils.{ discreteCell, estimateCoverage, estimateRealCoverage }
import utils.types.CircularBuffer

object ExplorationReward:

  private val logger = Logger(getClass.getName)

  case class ExplorationState(
      var ticks: Int = 0,
      positions: List[Point2D] = List.empty,
      actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      visitedCells: Set[(Int, Int)] = Set.empty,
      achievedMilestones: Set[Int] = Set.empty,
      visitedCounts: Map[(Int, Int), Int] = Map.empty,
      lastCoverage: Double = 0.0,
      maxTicks: Int = 10_000,
  )

  private object ExplorationRewardStateManager extends RewardStateManager[Agent, ExplorationState]:
    override def createState(): ExplorationState = ExplorationState()

  final case class ExplorationQL() extends StatefulReward[Agent, ExplorationState]:

    private val StuckPenalty: Double = -1.0
    private val NotStuckBonus: Double = +0.5
    private val CollidingPenalty: Double = -100.0
    private val MilestoneBonus: Double = +5.0
    private val ExplorationBonus: Double = +5.0
    private val CoverageBonus: Double = +500.0
    private val NotBonus: Double = 0.0

    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
      ExplorationRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ExplorationState,
    ): (Double, ExplorationState) =

      val newTick = state.ticks + 1
      val updatedPositions = entity.position :: state.positions

      // stuck
      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
      val stuckReward = if isStuck then StuckPenalty else NotStuckBonus

      // collision
      val currentMin = distanceFromObstacle(current.environment, entity)
      val collidingReward = if currentMin < CollisionTriggerDistance then CollidingPenalty else NotBonus

      // exploration new cell
      val currentCell = discreteCell(entity.position, CellSize)
      val isNewCell = !state.visitedCells.contains(currentCell)
      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
      val explorationReward = if isNewCell then ExplorationBonus else NotBonus

      // bonus milestone coverage
      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
      val currentPercent = math.floor(coverage * Percent).toInt
      val achieved = state.achievedMilestones
      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
      val milestonesReward = newMilestones.map(m => m * MilestoneBonus).sum
      val updateMilestones = achieved ++ newMilestones

      // final bonus
      val completionReward: Double = if coverage >= CoverageThreshold then CoverageBonus else NotBonus

      val reward =
        milestonesReward + // +5.0 * milestone.size or 0.0
          completionReward + // +500.0 or 0.0
          collidingReward + // -100.0 or 0.0
          stuckReward + // -1.0 or 0.5
          explorationReward // +5.0 or 0.0

      logger.info(
        f"TICK [$newTick] colliding=$collidingReward stuck=$stuckReward coverage=$coverage exploration=$explorationReward " +
          f"milestone=$milestonesReward completion=$completionReward | reward=$reward ",
      )

      val newState = state.copy(
        ticks = newTick,
        actionHistory = state.actionHistory.add(action),
        positions = updatedPositions,
        visitedCells = updatedVisited,
        achievedMilestones = updateMilestones,
        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
      )
      (reward, newState)
    end compute
  end ExplorationQL

  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:

    private val DangerThreshold = 0.35
    private val StuckPenalty = -1.0
    private val CollisionHardPenalty = -100.0
    private val ExplorationBonus = +10.0
    private val MilestoneBonus = +2.0
    private val CoverageBonus = +500.0
    private val MaxVisitsPerCell = 10

    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
      ExplorationRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        agent: Agent,
        action: Action[?],
        state: ExplorationState,
    ): (Double, ExplorationState) =

      var rCollision = 0.0
      //      var rClearance = 0.0
      var rExploration = 0.0
      var rCountBasedExploration = 0.0
      var rMilestone = 0.0
      var rCompletion = 0.0
      //      var rCuriosity = 0.0
      var rStuck = 0.0

      val newTick = state.ticks + 1
      val updatedPos = agent.position :: state.positions
//      val prevAgent = getAgentFromId(agent, prev)
      val currentMin = distanceFromObstacle(current.environment, agent)

      val currentCell = discreteCell(agent.position, CellSize)
      val isNewCell = !state.visitedCells.contains(currentCell)
      val updatedCells = if isNewCell then state.visitedCells + currentCell else state.visitedCells

      val coverage = estimateRealCoverage(updatedCells, current.environment, agent.shape.radius, CellSize)
      val currentPercent = math.floor(coverage * Percent).toInt
      val achieved = state.achievedMilestones
      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet

//      val centroid = updatedPos.mean
//      val novelty = agent.position.distanceTo(centroid)

      val oldCount = state.visitedCounts.getOrElse(currentCell, 0)
      val updatedCounts: Map[(Int, Int), Int] = state.visitedCounts + (currentCell -> (oldCount + 1))

      if currentMin < CollisionTriggerDistance then rCollision = CollisionHardPenalty

//      rClearance = clearanceReward(prev.environment, current.environment, agent, -5)

      if isNewCell then rExploration = ExplorationBonus

      // COUNT-BASED EXPLORATION
      rCountBasedExploration = 1.0 / math.sqrt(oldCount + 1)

      if newMilestones.nonEmpty then rMilestone = newMilestones.map(m => (m.toDouble / Percent) * MilestoneBonus).sum

      if coverage >= CoverageThreshold then
        val scaled = (coverage - CoverageThreshold) / (1.0 - CoverageThreshold)
        rCompletion = CoverageBonus * math.min(1.0, math.max(0.0, scaled))

//      rCuriosity = novelty * 0.1

      if isAgentStuck(updatedPos, WindowStuck) || updatedCounts(currentCell) > MaxVisitsPerCell then rStuck = StuckPenalty

      val rMove = movementReward(action, currentMin, 0.35) // moveReward(agent, prevAgent, action, currentMin)

      val reward =
        rCollision +
          //          rClearance +
          (rMove * 0.5) +
          (rCountBasedExploration * 10) +
          rExploration +
          rMilestone +
          rCompletion +
          //          rCuriosity +
          rStuck

      logger.info(
        f"TICK [$newTick] reward=$reward | " +
          f"minDist=$currentMin%.2f " +
          f"avoid=${currentMin < DangerThreshold} " +
          f"colliding=$rCollision " +
          f"stuck=$rStuck " +
          f"coverage=$coverage " +
          f"countBasedExploration=${rCountBasedExploration * 5} " +
          f"exploration=$rExploration " +
          s"updatedCounts=$updatedCounts " +
          //          f"cur=$rCuriosity " +
          f"milestone=$rMilestone " +
          f"completion=$rCompletion " +
          f"move=${rMove * 0.5} ",
          //          f"clearance=$rClearance ",
      )

      val newState = state.copy(
        ticks = newTick,
        actionHistory = state.actionHistory.add(action),
        positions = updatedPos,
        visitedCells = updatedCells,
        achievedMilestones = achieved ++ newMilestones,
        visitedCounts = updatedCounts,
        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
      )

      (reward, newState)
    end compute

    def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id == agent.id => a }.getOrElse(agent)
  end ExplorationDQN

//  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    private val dangerThreshold = 0.35
//    private val stuckPenalty = -1.0
//    private val collisionHardPenalty = -1000.0
////    private val explorationBonus = +30.0
//    private val milestoneBonus = +2.0
//    private val coverageBonus = +500.0
//
//    override protected def stateManager : RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          agent: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      var rCollision = 0.0
////      var rClearance = 0.0
//      var rExploration = 0.0
//      var rMilestone = 0.0
//      var rCompletion = 0.0
////      var rCuriosity = 0.0
//      var rStuck = 0.0
//
//      val newTick = state.ticks + 1
//      val updatedPos = agent.position :: state.positions
//      val prevAgent = getAgentFromId(agent, prev)
//      val currentMin = distanceFromObstacle(current.environment, agent)
//
//      val currentCell = discreteCell(agent.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedCells = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//
//      val coverage = estimateRealCoverage(updatedCells, current.environment, agent.shape.radius, CellSize)
//      val currentPercent = math.floor(coverage * Percent).toInt
//      val achieved = state.achievedMilestones
//      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
//
////      val centroid = updatedPos.mean
////      val novelty = agent.position.distanceTo(centroid)
//
//      val oldCount = state.visitedCounts.getOrElse(currentCell, 0)
//      val updatedCounts: Map[(Int, Int), Int] = state.visitedCounts + (currentCell -> (oldCount + 1))
//
//
//      if currentMin < dangerThreshold then
//
//        if currentMin < CollisionTriggerDistance then
//          rCollision = collisionHardPenalty
//
////        rClearance = clearanceReward(prev.environment, current.environment, agent, -5)
//
//      else
//
////        if isNewCell then
////          rExploration = explorationBonus
//
//        // COUNT-BASED EXPLORATION
//        rExploration = 1.0 / math.sqrt(oldCount + 1)
//
//        if newMilestones.nonEmpty then
//          rMilestone = newMilestones.map(m => (m.toDouble / Percent) * milestoneBonus).sum
//
//        if coverage >= CoverageThreshold then
//          val scaled = (coverage - CoverageThreshold) / (1.0 - CoverageThreshold)
//          rCompletion = coverageBonus * math.min(1.0, math.max(0.0, scaled))
//
////        rCuriosity = novelty * 0.1
//
//
//      if isAgentStuck(updatedPos, WindowStuck) then
//        rStuck = stuckPenalty
//
//      val rMove = moveReward(agent, prevAgent, action, currentMin)
//
//      val reward =
//        rCollision +
////          rClearance +
//          rMove +
//          rExploration * 2 +
//          rMilestone +
//          rCompletion +
////          rCuriosity +
//          rStuck
//
//      logger.info(
//        f"TICK [$newTick] reward=$reward | " +
//          f"minDist=$currentMin%.2f " +
//          f"avoid=${currentMin < dangerThreshold} " +
//          f"colliding=$rCollision " +
//          f"stuck=$rStuck " +
//          f"coverage=$coverage " +
//          f"exploration=$rExploration " +
//          s"updatedCounts=$updatedCounts " +
////          f"cur=$rCuriosity " +
//          f"milestone=$rMilestone " +
//          f"completion=$rCompletion " +
//          f"move=$rMove "
////          f"clearance=$rClearance ",
//      )
//
//      val newState = state.copy(
//        ticks = newTick,
//        actionHistory = state.actionHistory.add(action),
//        positions = updatedPos,
//        visitedCells = updatedCells,
//        achievedMilestones = achieved ++ newMilestones,
//        visitedCounts = updatedCounts,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//
//      (reward, newState)
//    end compute
//
//    def getAgentFromId(agent: Agent, state: BaseState): Agent =
//      state.environment.entities.collectFirst { case a: Agent if a.id == agent.id => a }.getOrElse(agent)

  // ------

  //  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    private val StuckPenalty: Double = -1.0
//    //    private val NotStuckBonus: Double = +0.5
//    private val CollisionHardPenalty = -1000.0
//    // private val CollisionSoftPenalty = -1.0
//    private val MilestoneBonus: Double = +20.0
//    private val ExplorationBonus: Double = +30.0
//    private val CoverageBonus: Double = +500.0
//    private val NotBonus: Double = 0.0
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//        prev: BaseState,
//        current: BaseState,
//        entity: Agent,
//        action: Action[?],
//        state: ExplorationState,
//    ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // COLLISION
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      var collidingReward = NotBonus
//      var rClear = NotBonus
//      var rMove = NotBonus
//      if currentMin < 0.5 then
//        collidingReward = if currentMin < CollisionTriggerDistance then CollisionHardPenalty else NotBonus
//        rClear = clearanceReward(prev.environment, current.environment, entity, -5)
//        val prevAgent = getAgentFromId(entity, prev)
//        rMove = moveReward(entity, prevAgent, action, currentMin)
//
//      // STUCK
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckReward = if isStuck then StuckPenalty else NotBonus
//
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//
//      // EXPLORATION
//      val explorationReward = if isNewCell then ExplorationBonus else NotBonus
//
//      // MILESTONE
//      val coverage = estimateRealCoverage(updatedVisited, current.environment, entity.shape.radius, CellSize)
//      val currentPercent = math.floor(coverage * Percent).toInt
//      val achieved = state.achievedMilestones
//      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
//      val milestonesReward = newMilestones.map(m => (m.toDouble / Percent) * MilestoneBonus).sum
//      val updateMilestones = achieved ++ newMilestones
//
//      // COMPLETITION
//      val completionReward: Double =
//        if coverage >= CoverageThreshold then
//          val scaled = (coverage - CoverageThreshold) / (1.0 - CoverageThreshold)
//          CoverageBonus * math.min(1.0, math.max(0.0, scaled))
//        else NotBonus
//
//      // CURIOITY
////      val sensorVariance = variance(entity.senseAll[Id](current.environment).proximityReadings.map(_.value))
////      val curiosityReward = sensorVariance * 0.5
//      val centroid = updatedPositions.mean
//      val novelty = entity.position.distanceTo(centroid)
//      val curiosityReward = novelty * 0.1
//
//      val rawReward =
//        stuckReward +
//          explorationReward +
//          milestonesReward +
//          completionReward +
//          collidingReward +
//          rClear +
//          rMove +
//          //(collidingReward + rClear + rMove).max(-20).min(+20) +
//          curiosityReward
//
//      val reward = rawReward.max(-1.0).min(+1.0)
//
//      logger.info(
//        f"TICK [$newTick] reward=$reward | " +
//          f"colliding=$collidingReward " +
//          f"stuck=$stuckReward " +
//          f"coverage=$coverage " +
//          f"exploration=$explorationReward " +
//          f"milestone=$milestonesReward " +
//          f"completion=$completionReward " +
//          f"move=$rMove " +
//          f"clearance=$rClear ",
//      )
//
//      val newState = state.copy(
//        ticks = newTick,
//        actionHistory = state.actionHistory.add(action),
//        positions = updatedPositions,
//        visitedCells = updatedVisited,
//        achievedMilestones = updateMilestones,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//      (reward, newState)
//    end compute
//
//    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
//      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)
//
//  end ExplorationDQN

  private def movementReward(action: Action[?], currentMin: Double, dangerThreshold: Double): Double =
    action match
      case ma: MovementAction[?] =>
        val l = ma.leftSpeed
        val r = ma.rightSpeed

        val isForward = l > 0 && r > 0
        val isRotate = (l == 0 && r != 0) || (r == 0 && l != 0)

        if isForward then 0.2 // premio leggero sempre valido
        else if isRotate then
          if currentMin < dangerThreshold then 0.2 // ruotare aiuta a evitare
          else -0.1 // se lontano, ruotare non è utile → piccola penalità
        else 0.0

      case _ => 0.0

  def moveReward(agent: Agent, prevAgent: Agent, action: Action[?], currentMin: Double): Double =
    if agent.position == prevAgent.position then if currentMin < 0.1 then 0.5 else 0.0
    else
      action match
        case ma: MovementAction[?] if ma.leftSpeed == 0.0 || ma.rightSpeed == 0.0 =>
          if currentMin > 0.5 then -1.0 else 1.0
        case ma: MovementAction[?] if ma.leftSpeed > 0 && ma.rightSpeed > 0 => 2.0
        case _ => -1.0

  def clearanceReward(prev: Environment, current: Environment, entity: Agent, base: Double): Double =
    val prevMin = distanceFromObstacle(prev, entity)
    val currMin = distanceFromObstacle(current, entity)
    val delta = currMin - prevMin
    val rChange = delta * 10.0
    val rProximity = -exp(base * currMin)
    (rChange + rProximity) / 2

  private def distanceFromObstacle(env: Environment, entity: Agent): Double =
    val agent =
      env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
    agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => math.min(acc, sr.value))

  private def isAgentStuck(positions: List[Point2D], window: Int, tolerance: Double = 0.01): Boolean =
    if positions.sizeIs >= window then
      val recentPositions = positions.take(window)
      recentPositions.headOption.exists { head =>
        recentPositions.forall(p => head.distanceTo(p) < tolerance)
      }
    else false

  def variance(xs: Seq[Double]): Double =
    if xs.isEmpty then 0.0
    else
      val mean = xs.sum / xs.size
      val meanSq = xs.map(x => x * x).sum / xs.size
      meanSq - mean * mean

end ExplorationReward
