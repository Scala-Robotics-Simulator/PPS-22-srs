package io.github.srs.testutils

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.environment.Environment

object EnvironmentTestUtils:

  def almostEqual(p1: Point2D, p2: Point2D, tol: Double = 0.5): Boolean =
    math.abs(p1.x - p2.x) <= tol && math.abs(p1.y - p2.y) <= tol

  def entitiesAlmostEqual(a: Entity, e: Entity, tol: Double): Boolean =
    val samePos = almostEqual(a.position, e.position, tol)
    val sameDims = (a, e) match
      case (ao: Obstacle, eo: Obstacle) =>
        math.abs(ao.width - eo.width) <= tol &&
        math.abs(ao.height - eo.height) <= tol
      case _ => true
    samePos && sameDims

  def envAlmostEqual(env: Environment, expected: Environment, tol: Double): Boolean =
    env.width == expected.width &&
      env.height == expected.height &&
      expected.entities.forall { e =>
        env.entities.exists(a => entitiesAlmostEqual(a, e, tol))
      }

  def neighborhood(pos: Point2D): Set[Point2D] =
    (for
      dx <- -1 to 1
      dy <- -1 to 1
    yield Point2D(pos.x.toInt + dx, pos.y.toInt + dy)).toSet

  extension (env: Environment)

    infix def shouldEqualExceptIds(expectedEnv: Environment, tol: Double = 0.5): Boolean =
      envAlmostEqual(env, expectedEnv, tol)

    infix def shouldEqualExceptIdsStrict(expectedEnv: Environment, tol: Double = 1e-6): Boolean =
      envAlmostEqual(env, expectedEnv, tol)
end EnvironmentTestUtils
