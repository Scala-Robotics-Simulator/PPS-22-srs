package io.github.srs.model.entity.dynamicentity.agent.reward

import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.Action
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

    private val StuckPenalty: Double = -1.0
//    private val NotStuckBonus: Double = +0.5
    private val CollisionHardPenalty = -100.0
    private val CollisionSoftPenalty = -1.0
    private val MilestoneBonus: Double = +5.0
    private val ExplorationBonus: Double = +5.0
    private val CoverageBonus: Double = +500.0
    private val NotBonus: Double = 0.0
    private val RewardClampMin = -5.0
    private val RewardClampMax = +5.0

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

      // -----------------------------------------------------------
      // 1. STUCK / LOOP
      // -----------------------------------------------------------

      // Se l’agente è fisicamente bloccato
      val isStuck = isAgentStuck(updatedPositions, WindowStuck)

      // Cella corrente
      val currentCell = discreteCell(entity.position, CellSize)

//      // Controllo loop: quante volte è stata visitata recentemente
//      val recentVisits = state.positions
//        .take(WindowStuck) // finestrella delle ultime posizioni
//        .map(pos => discreteCell(pos, CellSize))
//        .count(_ == currentCell)
//
//      // Penalità crescente se visita ripetuta
//      val loopPenalty = -recentVisits * 0.5 // più alto se resta in loop

      val stuckReward =
        if isStuck then StuckPenalty
//        else if recentVisits > WindowStuck then loopPenalty
        else 0.0 // NotStuckBonus

      // -----------------------------------------------------------
      // 2. COLLISIONE (incrementale, stessa logica if/else)
      // -----------------------------------------------------------
      val currentMin = distanceFromObstacle(current.environment, entity)
//
//      val collisionFactor =
//        math.min(1.0, math.max(0.0, currentMin / CollisionTriggerDistance))
//
//      val collidingReward =
//        if currentMin < CollisionTriggerDistance then
//          CollidingPenalty * (1.0 - collisionFactor) // da 0 a -100
//        else
//          NotBonus

      val safeDistance = CollisionTriggerDistance * 5
      val dangerDistance = CollisionTriggerDistance

      val dist = currentMin

      val avoidReward =
        if dist < dangerDistance then
          // molto vicino → penalità forte
          CollisionHardPenalty * (1.0 - (dist / dangerDistance))
        else if dist < safeDistance then
          // zona di rischio → piccola penalità proporzionale
          CollisionSoftPenalty * (1.0 - (dist / safeDistance)) // -0.1
        else
          // lontano → piccolo bonus per muoversi in zone libere
          NotBonus // +0.05

      // -----------------------------------------------------------
      // 3. NUOVA CELLA (incrementale ma logica identica)
      // -----------------------------------------------------------
      val isNewCell = !state.visitedCells.contains(currentCell)
      val updatedVisited =
        if isNewCell then state.visitedCells + currentCell else state.visitedCells

      val explorationReward =
        if isNewCell then ExplorationBonus else NotBonus // invariato, ma già incrementale possibile

      // -----------------------------------------------------------
      // 4. MILESTONE (incrementale, logica identica)
      // -----------------------------------------------------------
      val coverage =
        estimateRealCoverage(updatedVisited, current.environment, entity.shape.radius, CellSize)

      val currentPercent = math.floor(coverage * Percent).toInt
      val achieved = state.achievedMilestones
      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet

      val milestonesReward =
        newMilestones.map(m => (m.toDouble / Percent) * MilestoneBonus).sum

      val updateMilestones = achieved ++ newMilestones

      // -----------------------------------------------------------
      // 5. COMPLETION BONUS (incrementale, stessa logica)
      // -----------------------------------------------------------
      val completionReward: Double =
        if coverage >= CoverageThreshold then
          val scaled = (coverage - CoverageThreshold) / (1.0 - CoverageThreshold)
          CoverageBonus * math.min(1.0, math.max(0.0, scaled))
        else NotBonus

      // -----------------------------------------------------------
      // 6. TOTAL REWARD (identica combinazione)
      // -----------------------------------------------------------
      val rawReward =
        milestonesReward +
          completionReward +
          avoidReward +
          stuckReward +
          explorationReward

      val reward =
        math.max(RewardClampMin, math.min(RewardClampMax, rawReward))

      logger.info(
        f"TICK [$newTick] colliding=$avoidReward stuck=$stuckReward coverage=$coverage " +
          f"exploration=$explorationReward milestone=$milestonesReward completion=$completionReward | reward=$reward",
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
  end ExplorationDQN

//------------------------------------------------------
  //  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    private val StuckPenalty: Double = -1.0
//    private val NotStuckBonus: Double = +0.5
//    private val CollidingPenalty: Double = -100.0
//    private val MilestoneBonus: Double = +5.0
//    private val ExplorationBonus: Double = +5.0
//    private val CoverageBonus: Double = +500.0
//    private val NotBonus: Double = 0.0
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // stuck
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckReward = if isStuck then StuckPenalty else NotStuckBonus
//
//      // collision
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      val collidingReward = if currentMin < CollisionTriggerDistance then CollidingPenalty else NotBonus
//
//      // exploration new cell
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//      val explorationReward = if isNewCell then ExplorationBonus else NotBonus
//
//      // bonus milestone coverage
////      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val coverage = estimateRealCoverage(updatedVisited, current.environment, entity.shape.radius, CellSize)
//
//      val currentPercent = math.floor(coverage * Percent).toInt
//      val achieved = state.achievedMilestones
//      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
//      val milestonesReward = newMilestones.map(m => m * MilestoneBonus).sum
//      val updateMilestones = achieved ++ newMilestones
//
//      // final bonus
//      val completionReward: Double = if coverage >= CoverageThreshold then CoverageBonus else NotBonus
//
//      val reward =
//        milestonesReward + // +5.0 * milestone.size or 0.0
//          completionReward + // +500.0 or 0.0
//          collidingReward + // -100.0 or 0.0
//          stuckReward + // -1.0 or 0.5
//          explorationReward // +5.0 or 0.0
//
//      logger.info(
//        f"TICK [$newTick] colliding=$collidingReward stuck=$stuckReward coverage=$coverage exploration=$explorationReward " +
//          f"milestone=$milestonesReward completion=$completionReward | reward=$reward ",
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
//  end ExplorationDQN

  // -----------------------------------------------------------------------------------------------------------------

//  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    private val StuckPenalty: Double = -1.0
//    private val NotStuckBonus: Double = +0.1
//    private val CollidingPenalty: Double = -5.0
//    private val ExplorationScaling: Double = 0.5
//    private val CoverageScaling: Double = 1.0
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // ---- STUCK ----
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckReward = if isStuck then StuckPenalty else NotStuckBonus
//
//      // ---- COLLISION ----
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      val collidingReward = if currentMin < CollisionTriggerDistance then CollidingPenalty else 0.0
//
//      // ---- EXPLORATION ----
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//      val explorationReward = if isNewCell then ExplorationScaling else 0.0
//
//      // ---- COVERAGE ----
//      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val coverageReward = (coverage - state.lastCoverage) * CoverageScaling
//      val newState = state.copy(lastCoverage = coverage)
//
//      // ---- FINAL REWARD ----
//      val reward = stuckReward + collidingReward + explorationReward + coverageReward
//
//      logger.info(
//        f"TICK [$newTick] colliding=$collidingReward stuck=$stuckReward coverage=$coverage coverageReward=$coverageReward exploration=$explorationReward reward=$reward"
//      )
//
//      val updatedState = newState.copy(
//        ticks = newTick,
//        positions = updatedPositions,
//        actionHistory = state.actionHistory.add(action),
//        visitedCells = updatedVisited,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//
//      (reward, updatedState)
//    end compute
//  end ExplorationDQN

//  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//
//      val avoidObstacleReward = ObstacleAvoidanceRewardModule.
//        DQObstacleAvoidance().evaluate(prev, current, entity, action)
//
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//      val explorationReward = if isNewCell then +1.0 else -0.05
//
//
//      val coverageCurrent = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val coveragePrev = estimateCoverage(state.visitedCells, prev.environment, CellSize)
//
//      val coverageDelta = coverageCurrent - coveragePrev
//      val coverageReward = coverageDelta * 5.0
//
//      val reward = if coverageCurrent >= 0.80 then +200.0 else
//        1.0 * avoidObstacleReward + 2.0 * (explorationReward + coverageReward)
//
//      logger.info(
//        f"[DQN] reward:\t $reward | OA:\t $avoidObstacleReward | EXP:\t $explorationReward | COV:\t $coverageReward"
//      )
//
//      val newState = state.copy(
//        visitedCells = updatedVisited,
//      )
//      (reward, newState)

//  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // --- 1️⃣ STUCK DETECTION ---
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckReward = if (isStuck) -1.0 else 0.1
//
//      // --- 2️⃣ COLLISION / OSTACOLI ---
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      val safeDistance = 2.5 * CollisionTriggerDistance
//      val minDistance = CollisionTriggerDistance
//
//      val collisionPenalty =
//        if currentMin < minDistance then -1.0
//        else if currentMin < safeDistance then -(safeDistance - currentMin) / safeDistance
//        else 0.0
//
//      // --- 3️⃣ MOVIMENTO UTILE ---
//      val lastPos = state.positions.headOption.getOrElse(entity.position)
//      val stepDist = entity.position.distanceTo(lastPos)
//      val movementReward = if stepDist < 0.01 then -0.1 else math.min(stepDist * 2.0, 0.2)
//
//      // --- 4️⃣ EXPLORATION / NUOVE CELLE ---
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//      val explorationReward = if isNewCell then 0.5 else 0.0
//
//      // --- 5️⃣ DELTA COVERAGE ---
//      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val deltaCoverage = coverage - state.lastCoverage
//      val coverageReward = math.max(0.0, deltaCoverage * 1.0) // scala il delta coverage
//
//      // --- 6️⃣ COMPLETION BONUS ---
//      val completionReward = if coverage >= CoverageThreshold then 1.0 else 0.0
//
//      // --- 7️⃣ REPETITIVE ACTION PENALTY ---
//      val sameActionPenalty = state.actionHistory.lastOption match
//        case Some(lastAction) if lastAction == action => -0.05
//        case _ => 0.0
//
//      // --- 8️⃣ CALCOLO REWARD FINALE ---
//      var reward = stuckReward + collisionPenalty + movementReward +
//        explorationReward + coverageReward + completionReward +
//        sameActionPenalty
//
//      // Clip reward per stabilità
//      reward = math.max(-1.0, math.min(1.0, reward))
//
//      // --- 9️⃣ LOG ---
//      logger.info(
//        f"[DQN] tick=$newTick reward=$reward%1.3f cov=$coverage%1.3f cov=$deltaCoverage%1.3f newCell=$isNewCell " +
//          f"stuck=$stuckReward collision=$collisionPenalty move=$movementReward"
//      )
//
//      // --- 🔟 UPDATE STATE ---
//      val newState = state.copy(
//        ticks = newTick,
//        positions = updatedPositions,
//        actionHistory = state.actionHistory.add(action),
//        visitedCells = updatedVisited,
//        lastCoverage = coverage,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//      (reward, newState)

  //  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // --- 1️⃣ STUCK DETECTION ---
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckReward = if isStuck then -0.5 else +0.05
//
//      // --- 2️⃣ COLLISION / VICINANZA OSTACOLI ---
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      val safeDistance = 3.0 * CollisionTriggerDistance
//      val minSafeDistance = CollisionTriggerDistance
//
//      // penalità più forte quando troppo vicino al muro
//      val wallPenalty =
//        if currentMin < minSafeDistance then -10.0
//        else if currentMin < safeDistance then -math.pow(safeDistance - currentMin, 2) * 5.0
//        else 0.0
//
//      // premio se l'agente si allontana dal muro rispetto al tick precedente
//      val prevMin = state.positions.headOption
//        .map(_ => distanceFromObstacle(current.environment, entity))
//        .getOrElse(currentMin)
//      val wallMoveBonus = if currentMin > prevMin then +1.0 else 0.0
//
//      // penalità aggiuntiva se troppo vicino al muro (rinforzo)
//      val closeToWallPenalty =
//        if currentMin < minSafeDistance then -math.pow(minSafeDistance - currentMin, 2) * 5.0
//        else 0.0
//
//      // --- 3️⃣ EXPLORATION NUOVE CELLE ---
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//      val explorationReward = if isNewCell then +5.0 else 0.0
//
//      // --- 4️⃣ BONUS MILESTONES ---
//      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val currentPercent = math.floor(coverage * Percent).toInt
//      val achieved = state.achievedMilestones
//      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
//      val milestonesReward = newMilestones.map(_ * 0.5).sum
//      val updateMilestones = achieved ++ newMilestones
//
//      // --- 5️⃣ BONUS COMPLETAMENTO COPERTURA ---
//      val completionReward = if coverage >= CoverageThreshold then +5.0 else 0.0
//
//      // --- 6️⃣ PENALITÀ AZIONI RIPETITIVE / BLOCCO ---
//      val sameActionPenalty = state.actionHistory.lastOption match
//        case Some(lastAction) if lastAction == action && currentMin < safeDistance => -0.5
//        case Some(lastAction) if lastAction == action => -0.05
//        case _ => 0.0
//
//      val stuckLoopPenalty =
//        if state.positions.take(3).forall(_ == entity.position) then -1.0
//        else 0.0
//
//      // --- 7️⃣ CALCOLO REWARD FINALE ---
//      var reward =
//        stuckReward +
//          wallPenalty +
//          wallMoveBonus +
//          closeToWallPenalty +
//          explorationReward +
//          milestonesReward +
//          completionReward +
//          sameActionPenalty +
//          stuckLoopPenalty
//
//      // clipping reward per stabilità
//      reward = math.max(-10.0, math.min(10.0, reward))
//
//      // --- 8️⃣ LOG ---
//      logger.info(
//        f"[DQN] tick=$newTick reward=$reward%2.3f coverage=$coverage%1.3f newCell=$isNewCell " +
//          f"wallPenalty=$wallPenalty wallMoveBonus=$wallMoveBonus closeToWallPenalty=$closeToWallPenalty " +
//          f"exploration=$explorationReward milestones=$milestonesReward sameActionPenalty=$sameActionPenalty stuckLoopPenalty=$stuckLoopPenalty"
//      )
//
//      // --- 9️⃣ UPDATE STATE ---
//      val newState = state.copy(
//        ticks = newTick,
//        positions = updatedPositions,
//        actionHistory = state.actionHistory.add(action),
//        visitedCells = updatedVisited,
//        lastCoverage = coverage,
//        achievedMilestones = updateMilestones,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//
//      (reward, newState)
//    end compute

  //  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:
//
//    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
//      ExplorationRewardStateManager
//
//    override def compute(
//                          prev: BaseState,
//                          current: BaseState,
//                          entity: Agent,
//                          action: Action[?],
//                          state: ExplorationState,
//                        ): (Double, ExplorationState) =
//
//      val newTick = state.ticks + 1
//      val updatedPositions = entity.position :: state.positions
//
//      // --- 1️⃣ DISTANZA DALL’OSTACOLO / MURO ---
//      val currentMin = distanceFromObstacle(current.environment, entity)
//      val safeDistance = 2.5 * CollisionTriggerDistance
//
//      // Penalità forte se troppo vicino
//      val nearWallPenalty =
//        if currentMin < CollisionTriggerDistance then -3.0
//        else if currentMin < safeDistance then -1.0
//        else 0.0
//
//      // --- 2️⃣ MOVIMENTO REALE ---
//      val lastPos = state.positions.headOption.getOrElse(entity.position)
//      val stepDist = entity.position.distanceTo(lastPos)
//
//      val stillPenalty =
//        if stepDist < 0.01 then -0.5 else 0.0 // fermo
//      val repetitivePenalty =
//        if state.positions.take(3).distinct.sizeIs <= 2 then -0.3 else 0.0 // oscillazioni
//      val usefulMoveBonus =
//        if stepDist >= 0.05 && currentMin > safeDistance then +0.3 else 0.0
//
//      val movementReward = stillPenalty + repetitivePenalty + usefulMoveBonus + nearWallPenalty
//
//      // --- 3️⃣ EXPLORATION / COVERAGE ---
//      val currentCell = discreteCell(entity.position, CellSize)
//      val isNewCell = !state.visitedCells.contains(currentCell)
//      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//
//      val explorationReward = if isNewCell then +1.0 else 0.0
//
//      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
//      val deltaCoverage = coverage - state.lastCoverage
//      val coverageReward = math.max(0.0, deltaCoverage * 30.0)
//
//      // --- 4️⃣ COVERAGE COMPLETION ---
//      val completionReward = if coverage >= CoverageThreshold then +10.0 else 0.0
//
//      // --- 5️⃣ STUCK DETECTION ---
//      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
//      val stuckPenalty = if isStuck then -0.8 else 0.0
//
//      // --- 6️⃣ REPETITIVE ACTION PENALTY ---
//      val sameActionPenalty =
//        state.actionHistory.lastOption match
//          case Some(lastAction) if lastAction == action => -0.05
//          case _ => 0.0
//
//      // --- 7️⃣ COVERAGE MOMENTUM ---
//      val noProgressPenalty =
//        if deltaCoverage == 0.0 && newTick % 100 == 0 then -0.5 else 0.0
//
//      // --- 8️⃣ TOTALE REWARD ---
//      var reward =
//        movementReward +
//          explorationReward +
//          coverageReward +
//          completionReward +
//          stuckPenalty +
//          sameActionPenalty +
//          noProgressPenalty
//
//      reward = math.max(-10.0, math.min(10.0, reward)) // clipping
//
//      // --- 9️⃣ LOG DETTAGLIATO ---
//      logger.info(
//        f"[DQN] tick=$newTick reward=$reward%2.3f cov=$coverage%1.3f Δcov=$deltaCoverage%1.3f " +
//          f"newCell=$isNewCell move=$stepDist%1.3f wall=$currentMin%1.3f"
//      )
//
//      // --- 🔟 UPDATE STATE ---
//      val newState = state.copy(
//        ticks = newTick,
//        positions = updatedPositions,
//        actionHistory = state.actionHistory.add(action),
//        visitedCells = updatedVisited,
//        lastCoverage = coverage,
//        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
//      )
//      (reward, newState)

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

end ExplorationReward
