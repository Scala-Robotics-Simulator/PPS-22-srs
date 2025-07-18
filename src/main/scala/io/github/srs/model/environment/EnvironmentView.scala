package io.github.srs.model.environment

import io.github.srs.model.*
import io.github.srs.model.StaticEntity.*

/**
 * All the per‑tick world data in one place:
 * • grid dims
 * • static & dynamic obstacles
 * • static & carried lights
 * • the FOV resistance grid
 * • the robots themselves
 */
final case class EnvironmentView(
                                  width:       Int,
                                  height:      Int,
                                  obstacles:   Set[Cell],
                                  lights:      Vector[StaticEntity.Light],
                                  resistance:  Array[Array[Double]],
//                                  robots:      Vector[Robot]     // ← new field
                                )

object EnvironmentView:

  /** Static environment only—no robots yet. */
  def static(env: Environment): EnvironmentView =
    val W = env.width.toInt
    val H = env.height.toInt

    val obs = env.entities.collect { case o: Obstacle =>
      val tl = o.pos.toCell
      for dx <- 0 until o.width; dy <- 0 until o.height
        yield Cell(tl.x + dx, tl.y + dy)
    }.flatten

    val lts = env.entities.collect { case l: Light => l }.toVector

    val res = Array.fill(W, H)(0.0)
    obs.foreach(c => res(c.x)(c.y) = 1.0)

    EnvironmentView(W, H, obs, lts, res)


extension (env: Environment)
  /** View of the static world (no robots yet). */
  def view: EnvironmentView = EnvironmentView.static(env)
