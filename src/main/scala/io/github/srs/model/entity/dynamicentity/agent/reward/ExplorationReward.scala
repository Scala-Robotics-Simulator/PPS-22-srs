package io.github.srs.model.entity.dynamicentity.agent.reward

import com.typesafe.scalalogging.Logger
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment

object ExplorationReward:
  private val maxTicks: Int = 10_000
  private val logger = Logger(getClass.getName)

  final case class ExplorationState(
      visited: Set[Point2D],
      last: Option[Point2D],
      ticks: Int,
  )

  protected var state: ExplorationState = ExplorationState(visited = Set(), last = None, ticks = 0)

  case class Exploration() extends RewardModel[Agent]:

    private val rewardForNewCell = 10.0
    private val penaltyForOldCell = -5.0
    private val penaltyForStayingStill = -5.0
    private val stepReward = 1.0

    override def evaluate(
        prev: Environment,
        current: Environment,
        entity: Agent,
        action: Action[?],
    ): Double =
      if entity.aliveSteps == 1 then restoreState(entity)

      val currentPos = entity.position
      val lastPos = state.last
      var explorationReward = stepReward

      explorationReward += newlyVisited(currentPos)
      explorationReward += stayingStill(lastPos, currentPos)
      explorationReward += coverage(current, currentPos)
      explorationReward += survivalReward(state.ticks, maxTicks)
      val totalReward = explorationReward

      state = state.copy(
        visited = state.visited + currentPos,
        last = Some(currentPos),
        ticks = state.ticks + 1,
      )

      logger.info(s"[ExplorationReward] currentPosition=$currentPos")
      logger.info(s"[ExplorationReward] explorationReward=$explorationReward")
      logger.info(s"[ExplorationReward] totalReward=$totalReward")
      logger.info(s"[ExplorationReward] visitedCount=${state.visited.size}")

      totalReward
    end evaluate

    private def coverage(current: Environment, currentPos: Point2D): Double =
      val prevVisitedCount = state.visited.size
      val newVisitedCount = state.visited + currentPos
      val newlyCoveredCells = newVisitedCount.size - prevVisitedCount
      val totalCells = current.width * current.height
      val coverageReward = 10.0 * newlyCoveredCells.toDouble / totalCells
      coverageReward

    private def newlyVisited(currentPos: Point2D): Double =
      val newlyVisited = !state.visited.contains(currentPos)
      if newlyVisited then rewardForNewCell else penaltyForOldCell

    private def stayingStill(lastPos: Option[Point2D], currentPos: Point2D): Double =
      if lastPos.contains(currentPos) then penaltyForStayingStill else 0.0

    private def restoreState(entity: Agent): Unit =
      state = ExplorationState(visited = Set(entity.position), last = None, ticks = 0)

    private def survivalReward(duration: Int, maxDuration: Int): Double =
      val t = duration.toDouble.max(1.0)
      val K = 0.1
      val norm = math.log(1.0 + maxDuration)
      val totalSoFar = K * math.log(1.0 + t) / norm
      val totalPrev = K * math.log(1.0 + (t - 1.0)) / norm
      val rSurvStep = totalSoFar - totalPrev
      rSurvStep
  end Exploration
end ExplorationReward
