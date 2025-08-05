package io.github.srs.model.entity.dynamicentity.action

import cats.Monad
import cats.syntax.foldable.toFoldableOps
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.RobotAction

private final case class CompositeAction[F[_]: Monad](actions: List[Action[F]]) extends Action[F]:

  override def run(robot: Robot)(using ra: RobotAction[F]): F[Robot] =
    actions.foldLeftM(robot)((r, act) => act.run(r))

final case class NoAction[F[_]: Monad]() extends Action[F]:

  override def run(robot: Robot)(using ra: RobotAction[F]): F[Robot] =
    Monad[F].pure(robot)

object ActionDsl:

  extension [F[_]: Monad](a: Action[F])
    infix def thenDo(next: Action[F]): Action[F] = CompositeAction(List(a, next))
    infix def thenDo(next: List[Action[F]]): Action[F] = CompositeAction(a :: next)
