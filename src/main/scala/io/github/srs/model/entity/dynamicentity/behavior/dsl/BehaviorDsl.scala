package io.github.srs.model.entity.dynamicentity.behavior.dsl

import scala.annotation.targetName

import cats.*
import cats.data.Kleisli
import cats.syntax.all.*
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.{ Behavior, Condition, Rule }
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment

/**
 * Core DSL for defining behaviors and rules in the policy.
 */
private object DslCore:

  extension [I, A, F[_]: Applicative](cond: Condition[I])

    /**
     * Builds a [[Rule]] This creates an implication: when the condition is satisfied, the action is executed.
     *
     * @param act
     *   The action to execute when the condition is satisfied (call-by-name for lazy evaluation)
     * @return
     *   A Rule that returns Some(act) if condition holds, None otherwise
     */
    @targetName("implies")
    infix def ==>(act: => A): Rule[F, I, A] =
      Kleisli(i => (if cond(i) then Some(act) else None).pure[F])

  extension [I, A, F[_]: Monad](r1: Rule[F, I, A])

    /**
     * First-win composition operator for combining rules. If `r1` produces `Some(action)`, it is returned; otherwise,
     * `r2` is tried. This implements a fallback strategy where rules are evaluated in order.
     *
     * @param r2
     *   The fallback rule to try if r1 doesn't produce an action
     * @return
     *   A combined rule that tries r1 first, then r2 if r1 fails
     */
    @targetName("orElse")
    infix def |(r2: Rule[F, I, A]): Rule[F, I, A] =
      Kleisli(i => r1.run(i).flatMap(_.fold(r2.run(i))(_.some.pure[F])))

    /**
     * Transform the decided action. This allows you to map the output of the rule to a different type.
     *
     * @param f
     *   A function to transform the action
     * @tparam B
     *   The new output type
     * @return
     *   A Rule with the transformed action
     */
    def mapAction[B](f: A => B): Rule[F, I, B] =
      r1.map(_.map(f))

    /**
     * Gate the rule with an extra input predicate.
     *
     * @param p
     *   An additional condition to check
     * @return
     *   A Rule that only applies if the additional condition holds
     */
    def onlyIf(p: Condition[I]): Rule[F, I, A] =
      Kleisli(i => r1.run(i).map(opt => if p(i) then opt else None))

  end extension

  extension [I, A, F[_]: Functor](rules: Rule[F, I, A])

    /**
     * Collapses a chain of rules into a [[Behavior]] that **always** returns an action, falling back if no rule fired.
     * This ensures the behavior is total and always produces an action.
     *
     * @param fallback
     *   The default action to use when no rules match (call-by-name for lazy evaluation)
     * @return
     *   A Behavior that guarantees to always return an action
     */
    def default(fallback: => A): Behavior[F, I, A] =
      Kleisli(i => rules.run(i).map(_.getOrElse(fallback)))

  extension [E <: DynamicEntity, Env <: Environment](s: ProximitySensor[E, Env])

    /**
     * Try to extract the reading from the `SensorReadings`. Searches through the sensor readings to find a matching
     * proximity sensor.
     *
     * @param rs
     *   The sensor readings collection to search through
     * @return
     *   Some(value) if a matching reading is found, None otherwise
     */
    private def reading(rs: SensorReadings): Option[Double] =
      rs.collectFirst { case SensorReading(ss: ProximitySensor[?, ?], v: Double) if ss eq s => v }

    /**
     * Creates a [[Condition]] that checks if the sensor reading is less than `t`.
     *
     * @param t
     *   The threshold value to compare against
     * @return
     *   A Condition function that returns true if sensor reading < t
     * @example
     *   `frontSensor < 0.3`
     */
    @targetName("lessThan")
    infix def <(t: Double): Condition[SensorReadings] =
      rs => reading(rs).exists(_ < t)

    /**
     * Creates a [[Condition]] that checks if the sensor reading is greater than `t`.
     *
     * @param t
     *   The threshold value to compare against
     * @return
     *   A Condition function that returns true if sensor reading > t
     * @example
     *   `frontSensor > 0.3`
     */
    @targetName("greaterThan")
    infix def >(t: Double): Condition[SensorReadings] =
      rs => reading(rs).exists(_ > t)

  end extension

  extension [I](c: Condition[I])

    /**
     * Logical AND between two conditions.
     *
     * @param d
     *   The second condition to combine
     * @return
     *   A Condition that is true if both conditions are true
     */
    infix def and(d: Condition[I]): Condition[I] = i => c(i) && d(i)

    /**
     * Logical OR between two conditions.
     *
     * @param d
     *   The second condition to combine
     * @return
     *   A Condition that is true if at least one condition is true
     */
    infix def or(d: Condition[I]): Condition[I] = i => c(i) || d(i)

    /**
     * Logical NOT of a condition.
     *
     * @return
     *   A Condition that is true if the original condition is false
     */
    def not: Condition[I] = i => !c(i)
  end extension

end DslCore

/**
 * Public facade for the behavior DSL.
 */
object dsl:

  export BehaviorTypes.Condition
  export DslCore.{ ==>, |, default, mapAction, onlyIf }
  export DslCore.{ and, not, or }
  export DslCore.{ <, > }
