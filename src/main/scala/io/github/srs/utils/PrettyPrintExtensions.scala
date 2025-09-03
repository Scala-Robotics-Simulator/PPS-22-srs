package io.github.srs.utils

import cats.Id
import io.github.srs.model.ModelModule
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.environment.Environment

object PrettyPrintExtensions:

  extension (r: Robot)

    /**
     * Pretty prints the actuators of the robot.
     * @return
     *   a string representation of the robot's actuators.
     */
    private def prettyPrintActuators: String =
      if r.actuators.isEmpty then "None"
      else
        r.actuators.map {
          case m: DifferentialWheelMotor =>
            s"DifferentialWheelMotor -> Left: ${m.left.speed}, Right: ${m.right.speed}"
          case a =>
            s"${a.getClass.getSimpleName}"
        }.mkString("\n  ")

    /**
     * Pretty prints the robot's details including its ID, position, orientation, shape, actuators, and sensor readings.
     * @param env
     *   the environment in which the robot operates, used to get sensor readings.
     * @return
     *   a formatted string representation of the robot's details.
     */
    def prettyPrint(env: Environment): String =
      s"""--- Robot ---
         |ID: ${r.id}
         |Position: (${r.position.x}, ${r.position.y})
         |Orientation: ${r.orientation.degrees}°
         |Shape: Circle with radius ${r.shape.radius}
         |Actuators:
         |  ${r.prettyPrintActuators}
         |Sensors:
         |  ${SensorReadings.prettyPrint(r.senseAll[Id](env)).mkString("\n  ")}""".stripMargin

  end extension

  extension (env: Environment)

    /**
     * Pretty prints all robots in the environment.
     * @return
     *   a string representation of all robots in the environment.
     */
    def prettyPrint: String =
      val robots = env.entities.collect { case r: Robot => r.prettyPrint(env) }
      if robots.isEmpty then "  None"
      else robots.mkString("\n\n")

  extension (state: ModelModule.State)

    /**
     * Pretty prints the simulation state including time, speed, RNG seed, status, and environment details.
     * @return
     *   a formatted string representation of the simulation state.
     */
    def prettyPrint: String =
      s"""--- SimulationState ---
         |Simulation Time : ${state.simulationTime.map(t => s"${t.toMillis} ms").getOrElse("∞")}
         |Elapsed Time    : ${state.elapsedTime.toMillis} ms
         |Δt              : ${state.dt.toMillis} ms
         |Speed           : ${state.simulationSpeed}
         |RNG Seed        : ${state.simulationRNG}
         |Status          : ${state.simulationStatus}
         |
         |--- Environment ---
         |Width: ${state.environment.width}
         |Height: ${state.environment.height}
         |
         |${state.environment.prettyPrint}
         |-----------------------""".stripMargin
  end extension
end PrettyPrintExtensions
