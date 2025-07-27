package io.github.srs.model.lighting

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }
import io.github.srs.model.environment.*

/**
 * An object that demonstrates a simulation of light diffusion within an environment containing lights and obstacles.
 *
 * This example uses the [[ShadowFovDiffuser]] to compute light propagation, including shadowing and attenuation, in a
 * grid-based environment. The environment consists of obstacles and light sources, both of which influence the
 * resulting light map. The final light map is visualized in both ASCII and numeric formats.
 *
 * The environment is defined with specific dimensions and populated with a set of entities such as obstacles and light
 * sources. Light diffusion is computed in discrete steps using a Field of View engine. The rendered output offers a
 * preview of the diffusion process.
 *
 * The object provides the following functionalities:
 *   - Initialization of an environment's grid dimensions and entities.
 *   - Calculation of light diffusion using a shadow Field of View (FOV) algorithm.
 *   - ASCII and numeric rendering of the resulting light intensity map.
 *
 * The built-in `run` method serves as the entry point and orchestrates the setup, computation, and display of the light
 * diffusion.
 */
object LightDemo:

  given cellSize: Double = 1.0

  @main def run(): Unit =

    val env = Environment(
      width = 10,
      height = 10,
      entities = Set(
        Obstacle((2.0, 1.0), Orientation(0), 1, 1),
        Obstacle((2.0, 2.0), Orientation(0), 1, 1),
        Obstacle((6.0, 4.0), Orientation(0), 1, 1),
        Light((0.0, 0.0), Orientation(0), 8.0, 1.0, 0.2),
        Light((9.0, 9.0), Orientation(0), 2.0, 1.0, 0.2),
      ),
    ).fold(
      err => sys.error(s"Invalid environment: $err"),
      env => env,
    )

    val view = env.view

    val diffuser = ShadowFovDiffuser()
    val initialMap = LightState.empty(view.width, view.height)
    val lightMap = diffuser.step(view)(initialMap)

    println(
      s"""|Environment summary
          |‑ Grid : ${env.width}×${env.height}
          |‑ Obstacles : ${env.entities.count { case _: Obstacle => true; case _ => false }}
          |‑ Lights    : ${env.entities.count { case _: Light => true; case _ => false }}
          |""".stripMargin,
    )

    println("Visualizing ASCII:")
    println("-" * 30)
    println(lightMap.render(view))
    println()

    println("Visualizing numeric:")
    println("-" * 30)
    println(lightMap.render(view, ascii = false))

    println("\n" + "=" * 50)

    println("\n" + "=" * 50)

  end run
end LightDemo
