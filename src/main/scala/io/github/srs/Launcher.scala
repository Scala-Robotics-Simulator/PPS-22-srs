package io.github.srs

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }
import scala.util.Random

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.Path
import io.github.srs.config.{ SimulationConfig, YamlConfigManager }
import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.logic.simulationStateLogicsBundle
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.SimpleRNG
import io.github.srs.view.ViewModule
import io.github.srs.view.ViewModule.View
import io.github.srs.model.environment.dsl.CreationDSL.validate

/**
 * Launcher object that initializes the simulation.
 */
object Launcher
    extends ModelModule.Interface[SimulationState]
    with ViewModule.Interface[SimulationState]
    with ControllerModule.Interface[SimulationState]:

  val model: Model[SimulationState] = Model()
  val view: View[SimulationState] = View()
  val controller: Controller[SimulationState] = Controller()

  def run(config: SimulationConfig): IO[Unit] =
    val seed = config.simulation.seed.getOrElse(Random.nextLong())
    val duration = config.simulation.duration.map(FiniteDuration(_, MILLISECONDS))
    val environment = config.environment.validate.getOrElse(
      sys.exit(1),
    )
    for
      _ <- controller
        .start(
          SimulationState(
            i = 0,
            simulationTime = duration,
            simulationSpeed = SimulationSpeed.NORMAL,
            simulationRNG = SimpleRNG(seed),
            simulationStatus = SimulationStatus.RUNNING,
            environment = environment,
          ),
        )
      _ <- IO.never
    yield ()
  end run
end Launcher

object Main:

  @main def run(): Unit =
    val uri = getClass.getResource("/configuration.yml").toURI
    val path = Path.fromNioPath(java.nio.file.Paths.get(uri))
    val loader = YamlConfigManager[IO](path)
    val result = loader.load.unsafeRunSync()
    val config = result.getOrElse(
      sys.exit(1),
    )
    Launcher.run(config).unsafeRunSync()
