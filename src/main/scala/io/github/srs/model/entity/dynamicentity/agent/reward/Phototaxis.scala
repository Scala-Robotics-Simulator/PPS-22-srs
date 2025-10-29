package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.{ abs, exp, min }

import cats.Id
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.environment.Environment
import utils.types.CircularBuffer

/**
 * Phototaxis Reward Module
 */
object PhototaxisRewardModule:

  final protected case class PhototaxisState(
      var ticks: Int = 0,
      positions: CircularBuffer[Point2D] = CircularBuffer(200),
      actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      lightDistances: CircularBuffer[Double] = CircularBuffer(200),
      maxTicks: Int = 10_000,
  )

  object PhototaxisRewardStateManager extends RewardStateManager[Agent, PhototaxisState]:
    override def createState(): PhototaxisState = PhototaxisState()

  private val GoalReachedDistance = 0.5

  /**
   * Get the agent entity from the environment (handles entity lookup correctly).
   */
  private def getAgent(env: Environment, entity: Agent): Agent =
    env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)

  /**
   * Calculate distance to the nearest light source. Returns Double.MaxValue if no lights exist.
   */
  private def distanceToNearestLight(env: Environment, entity: Agent): Double =
    val agent = getAgent(env, entity)
    val lights = env.entities.collect { case l: Light => l }
    if lights.isEmpty then Double.MaxValue
    else lights.map(light => agent.position.distanceTo(light.position)).minOption.getOrElse(Double.MaxValue)

  /**
   * Calculate minimum proximity to obstacles using sensors.
   */
  private def minProximityToObstacles(env: Environment, entity: Agent): Double =
    val agent = getAgent(env, entity)
    agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

  /**
   * Standard obstacle avoidance reward. Penalty scales linearly when too close to obstacles.
   *
   * @param threshold
   *   Distance threshold
   * @param penaltyCoeff
   *   Penalty multiplier
   */
  private def obstacleAvoidancePenalty(
      env: Environment,
      entity: Agent,
      threshold: Double,
      penaltyCoeff: Double,
  ): Double =
    val proximity = minProximityToObstacles(env, entity)
    if proximity < threshold then -penaltyCoeff * (threshold - proximity) else 0.0

  /**
   * Movement reward/penalty for action smoothness. Penalizes spinning and oscillation, rewards forward movement.
   *
   * @param spinPenalty
   *   Penalty for spinning in place
   * @param oscillationCoeff
   *   Oscillation penalty multiplier
   * @param forwardBonus
   *   Bonus for smooth forward movement
   */
  private def movementReward(
      action: Action[?],
      state: PhototaxisState,
      spinPenalty: Double,
      oscillationCoeff: Double,
      forwardBonus: Double,
  ): Double =
    val movementActions = state.actionHistory.collect { case ma: MovementAction[?] => ma }

    val (leftSpeed, rightSpeed) = action match
      case ma: MovementAction[?] => (ma.leftSpeed, ma.rightSpeed)
      case _ => (0.0, 0.0)

    val isSpinning = (leftSpeed * rightSpeed < 0) && (abs(abs(leftSpeed) - abs(rightSpeed)) < 0.3)
    val rSpin = if isSpinning then -spinPenalty else 0.0

    val rOscillation = if movementActions.sizeIs >= 10 then
      val avgLeft = movementActions.map(_.leftSpeed).sum / movementActions.size
      val avgRight = movementActions.map(_.rightSpeed).sum / movementActions.size
      val deviation = abs(leftSpeed - avgLeft) + abs(rightSpeed - avgRight)
      if deviation > 0.5 then -deviation * oscillationCoeff else 0.0
    else 0.0

    val rForward = if leftSpeed > 0 && rightSpeed > 0 && abs(leftSpeed - rightSpeed) < 0.2 then forwardBonus else 0.0

    rSpin + rOscillation + rForward
  end movementReward

  /**
   * Update state with new tick data.
   */
  private def updateState(
      state: PhototaxisState,
      entity: Agent,
      action: Action[?],
      currentDist: Double,
      maxTicks: Int,
  ): PhototaxisState =
    state.copy(
      ticks = state.ticks + 1,
      positions = if state.ticks % 100 == 0 then state.positions.add(entity.position) else state.positions,
      actionHistory = state.actionHistory.add(action),
      lightDistances = if state.ticks % 10 == 0 then state.lightDistances.add(currentDist) else state.lightDistances,
      maxTicks = maxTicks,
    )

  /**
   * Navigates to light while avoiding obstacles.
   *
   * Components:
   *   - Progress reward: Distance reduction towards light
   *   - Proximity reward: Exponential bonus for being close to light (CRITICAL for guidance)
   *   - Goal bonus: Large reward (100.0) for reaching the light
   *   - Obstacle penalty: Standard collision avoidance
   *   - Movement penalties: Spinning (-1.0) and oscillation (-0.1x)
   *   - Survival reward: Logarithmic time-based reward
   */
  final case class Phototaxis() extends StatefulReward[Agent, PhototaxisState]:

    override protected def stateManager: RewardStateManager[Agent, PhototaxisState] =
      PhototaxisRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: PhototaxisState,
    ): (Double, PhototaxisState) =
      val prevDist = distanceToNearestLight(prev.environment, entity)
      val currentDist = distanceToNearestLight(current.environment, entity)

      val rProgress = (prevDist - currentDist) * 10.0

      val normalized = currentDist / 15.0
      val rProximity = exp(-3 * normalized)

      val rGoal = if currentDist < GoalReachedDistance then 100.0 else 0.0

      val rObstacle = obstacleAvoidancePenalty(current.environment, entity, threshold = 0.2, penaltyCoeff = 10.0)

      val rMove = movementReward(action, state, spinPenalty = 1.0, oscillationCoeff = 0.1, forwardBonus = 0.2)

      val t = state.ticks.toDouble.max(1.0)
      val K = 0.01
      val norm = math.log(1.0 + state.maxTicks)
      val rSurv = K * (math.log(1.0 + t) - math.log(t)) / norm

      val reward = rProgress + rProximity + rGoal + rObstacle + rMove + rSurv
      val maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000)

      (reward, updateState(state, entity, action, currentDist, maxTicks))
    end compute
  end Phototaxis

end PhototaxisRewardModule
