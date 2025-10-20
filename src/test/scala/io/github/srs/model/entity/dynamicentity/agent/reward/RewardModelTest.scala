package io.github.srs.model.entity.dynamicentity.agent.reward

import cats.effect.IO
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.action.{ Action, NoAction }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RewardModelTest extends AnyFlatSpec with Matchers:

  val testAgent: Agent = agent at Point2D(1.0, 1.0)
  val env1: Environment = Environment(10, 10)
  val env2: Environment = Environment(10, 10)
  val dummyAction: Action[IO] = NoAction[IO]()

  "NoReward" should "return zero reward for any transition" in:
    val reward = NoReward()
    val result = reward.evaluate(env1, env2, testAgent, dummyAction)
    result shouldBe 0.0

  it should "be instantiable with apply syntax" in:
    val reward = NoReward()
    reward shouldBe a[NoReward]

  it should "be instantiable as case class" in:
    val reward1 = NoReward()
    val reward2 = NoReward()
    reward1 shouldEqual reward2

  "RewardModel" should "have an evaluate method" in:
    val customReward = new RewardModel[Agent]:
      override def evaluate(
          prev: Environment,
          current: Environment,
          entity: Agent,
          action: Action[?],
      ): Double = 1.5

    val result = customReward.evaluate(env1, env2, testAgent, dummyAction)
    result shouldBe 1.5

  it should "support custom reward logic" in:
    val distanceReward = new RewardModel[Agent]:
      override def evaluate(
          prev: Environment,
          current: Environment,
          entity: Agent,
          action: Action[?],
      ): Double =
        val dist = math.sqrt(
          math.pow(entity.position._1 - 5.0, 2) + math.pow(entity.position._2 - 5.0, 2),
        )
        -dist // Negative distance as reward (closer to (5,5) is better)

    val nearAgent = agent at Point2D(5.0, 5.0)
    val farAgent = agent at Point2D(0.0, 0.0)

    val nearReward = distanceReward.evaluate(env1, env2, nearAgent, dummyAction)
    val farReward = distanceReward.evaluate(env1, env2, farAgent, dummyAction)

    nearReward should be > farReward

  "StatefulReward" should "maintain state across evaluations" in:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    val stepCountReward = new StatefulReward[Agent, Int]:
      protected var state: Int = 0

      override protected def compute(
          prev: Environment,
          curr: Environment,
          entity: Agent,
          action: Action[?],
          state: Int,
      ): (Double, Int) =
        val newState = state + 1
        (newState.toDouble, newState)

    val reward1 = stepCountReward.evaluate(env1, env2, testAgent, dummyAction)
    val reward2 = stepCountReward.evaluate(env1, env2, testAgent, dummyAction)
    val reward3 = stepCountReward.evaluate(env1, env2, testAgent, dummyAction)

    val _ = reward1 shouldBe 1.0
    val _ = reward2 shouldBe 2.0
    reward3 shouldBe 3.0

  it should "update state based on computation" in:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    val decayingReward = new StatefulReward[Agent, Double]:
      protected var state: Double = 10.0

      override protected def compute(
          prev: Environment,
          curr: Environment,
          entity: Agent,
          action: Action[?],
          currentState: Double,
      ): (Double, Double) =
        val newState = currentState * 0.9
        (newState, newState)

    val reward1 = decayingReward.evaluate(env1, env2, testAgent, dummyAction)
    val reward2 = decayingReward.evaluate(env1, env2, testAgent, dummyAction)

    val _ = reward1 shouldBe 9.0 +- 0.001
    reward2 shouldBe 8.1 +- 0.001

end RewardModelTest
