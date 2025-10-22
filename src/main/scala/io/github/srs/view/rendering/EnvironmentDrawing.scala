package io.github.srs.view.rendering

import java.awt.*
import java.awt.geom.Ellipse2D

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults
import io.github.srs.utils.SimulationDefaults.Canvas.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Sensor.ProximitySensor.DefaultRange
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import cats.Id
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.robot.Robot

/**
 * Viewport configuration for world-to-screen transformation.
 */
final case class Viewport(scale: Double, offsetX: Int, offsetY: Int, width: Int, height: Int)

/**
 * Shared rendering logic for both GUI and headless rendering. Can be mixed into components or used directly.
 */
trait EnvironmentDrawing:

  /**
   * Calculates viewport transformation for centering and scaling.
   *
   * @param env
   *   The environment to fit in the viewport
   * @param width
   *   Target width in pixels
   * @param height
   *   Target height in pixels
   * @return
   *   Viewport configuration
   */
  protected def calculateViewport(env: Environment, width: Int, height: Int): Viewport =
    val scale = math.min(width.toDouble / env.width, height.toDouble / env.height)
    val scaledWidth = (env.width * scale).toInt
    val scaledHeight = (env.height * scale).toInt
    val offsetX = (width - scaledWidth) / 2
    val offsetY = (height - scaledHeight) / 2
    Viewport(scale, offsetX, offsetY, scaledWidth, scaledHeight)

  /**
   * Draws the coordinate grid.
   *
   * @param g
   *   Graphics context
   * @param env
   *   Environment for dimensions
   * @param vp
   *   Viewport configuration
   */
  protected def drawGrid(g: Graphics2D, env: Environment, vp: Viewport): Unit =
    g.setColor(Color.LIGHT_GRAY)
    g.setStroke(new BasicStroke(GridStrokeWidth))

    (0 to env.width).foreach { x =>
      val sx = vp.offsetX + (x * vp.scale).toInt
      g.drawLine(sx, vp.offsetY, sx, vp.offsetY + vp.height)
    }
    (0 to env.height).foreach { y =>
      val sy = vp.offsetY + (y * vp.scale).toInt
      g.drawLine(vp.offsetX, sy, vp.offsetX + vp.width, sy)
    }

  /**
   * Draws coordinate labels with adaptive stepping.
   *
   * @param g
   *   Graphics context
   * @param env
   *   Environment for dimensions
   * @param vp
   *   Viewport configuration
   */
  protected def drawLabels(g: Graphics2D, env: Environment, vp: Viewport): Unit =
    g.setColor(Color.DARK_GRAY)
    val bottom = vp.offsetY + vp.height - LabelBottomOffset

    val step = adaptiveLabelStep(vp.scale)

    (0 to env.width by step).foreach { x =>
      g.drawString(x.toString, vp.offsetX + (x * vp.scale).toInt + LabelXOffset, bottom)
    }
    (0 to env.height by step).foreach { y =>
      val py = vp.offsetY + (y * vp.scale).toInt
      g.drawString(y.toString, vp.offsetX + LabelXOffset, Math.min(bottom, py + LabelYOffset))
    }

  /**
   * Calculates adaptive step size for labels based on zoom level.
   *
   * @param scale
   *   Current zoom scale
   * @return
   *   Step size for label spacing
   */
  private def adaptiveLabelStep(scale: Double): Int =
    val raw = LabelDesiredPx / scale
    val steps = LabelStepSequence
    LazyList
      .iterate(1)(_ * LabelScaleBase)
      .flatMap(m => steps.map(_ * m))
      .find(_ >= raw)
      .getOrElse(1)

  /**
   * Draws all static entities (obstacles and lights).
   *
   * @param g
   *   Graphics context
   * @param env
   *   Environment containing entities
   * @param vp
   *   Viewport configuration
   */
  protected def drawStaticEntities(g: Graphics2D, env: Environment, vp: Viewport): Unit =
    env.entities.foreach:
      case StaticEntity.Obstacle(_, pos, orientation, w, h) =>
        drawObstacle(g, pos, orientation.degrees, w, h, vp)
      case StaticEntity.Light(_, pos, _, radius, illuminationRadius, _, _) =>
        drawLight(g, pos, radius, illuminationRadius, vp)
      case _ => ()

  /**
   * Draws an obstacle with gradient fill.
   *
   * @param g
   *   Graphics context
   * @param pos
   *   Position in world coordinates
   * @param orientation
   *   Rotation in degrees
   * @param w
   *   Width of the obstacle
   * @param h
   *   Height of the obstacle
   * @param vp
   *   Viewport configuration
   */
  private def drawObstacle(
      g: Graphics2D,
      pos: Point2D,
      orientation: Double,
      w: Double,
      h: Double,
      vp: Viewport,
  ): Unit =
    import io.github.srs.utils.SimulationDefaults.UI.{ Colors, Strokes }

    val savedTransform = g.getTransform
    val centerX = vp.offsetX + pos.x * vp.scale
    val centerY = vp.offsetY + pos.y * vp.scale
    g.rotate(orientation.toRadians, centerX, centerY)

    val x = (vp.offsetX + (pos.x - w / 2) * vp.scale).toInt
    val y = (vp.offsetY + (pos.y - h / 2) * vp.scale).toInt
    val width = (w * vp.scale).toInt
    val height = (h * vp.scale).toInt

    val gradient = new GradientPaint(
      x.toFloat,
      y.toFloat,
      Colors.obstacleGradientStart,
      (x + width).toFloat,
      (y + height).toFloat,
      Colors.obstacleGradientEnd,
    )

    g.setPaint(gradient)
    g.fillRect(x, y, width, height)

    g.setColor(Colors.obstacleBorder)
    g.setStroke(new BasicStroke(Strokes.ObstacleStroke))
    g.drawRect(x, y, width, height)

    g.setTransform(savedTransform)

  end drawObstacle

  /**
   * Draws a light source with radial gradient.
   *
   * @param g
   *   Graphics context
   * @param pos
   *   Position in world coordinates
   * @param radius
   *   Radius of the light source
   * @param illuminationRadius
   *   Effective illumination radius
   * @param vp
   *   Viewport configuration
   */
  private def drawLight(
      g: Graphics2D,
      pos: Point2D,
      radius: Double,
      illuminationRadius: Double,
      vp: Viewport,
  ): Unit =
    val cx = (vp.offsetX + pos.x * vp.scale).toInt
    val cy = (vp.offsetY + pos.y * vp.scale).toInt

    val lightSize = math.max(LightFX.MinLightPixels, (DiameterFactor * radius * vp.scale).toInt)
    val illuminationSize = math.max(lightSize, (DiameterFactor * illuminationRadius * vp.scale).toInt)

    val gx = cx - illuminationSize / 2
    val gy = cy - illuminationSize / 2

    val gridLeft = vp.offsetX
    val gridTop = vp.offsetY
    val gridRight = vp.offsetX + vp.width
    val gridBottom = vp.offsetY + vp.height

    if gx < gridRight && gy < gridBottom && gx + illuminationSize > gridLeft && gy + illuminationSize > gridTop then
      val fractions = Array(
        LightFX.GradientFraction0,
        LightFX.GradientFraction1,
        LightFX.GradientFraction2,
        LightFX.GradientFraction3,
      )
      val colors = LightFX.GradientColor
      val glow = new RadialGradientPaint(
        new java.awt.geom.Point2D.Double(cx, cy),
        illuminationSize / LightFX.GradientRadiusDivisor,
        fractions,
        colors,
      )
      g.setPaint(glow)
      g.fill(new Ellipse2D.Double(gx, gy, illuminationSize, illuminationSize))

      val oldComp = g.getComposite
      g.setComposite(AlphaComposite.SrcOver.derive(LightFX.PostGlowAlpha))
      g.setColor(new Color(255, 170, 0))
      g.fill(new Ellipse2D.Double(gx, gy, illuminationSize, illuminationSize))
      g.setComposite(oldComp)

      g.setColor(new Color(255, 170, 0, 36))
      g.setStroke(
        new BasicStroke(
          LightFX.RingStrokeWidth,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          LightFX.RingStrokeMiterLimit,
          Array(LightFX.RingStrokeDash1, LightFX.RingStrokeDash2),
          LightFX.RingStrokeDashPhase,
        ),
      )
      g.drawOval(gx, gy, illuminationSize, illuminationSize)
    end if

    if cx >= gridLeft && cx <= gridRight && cy >= gridTop && cy <= gridBottom then
      val px = cx - lightSize / 2
      val py = cy - lightSize / 2
      val corePaint = new RadialGradientPaint(
        new java.awt.geom.Point2D.Double(cx, cy),
        lightSize / LightFX.GradientRadiusDivisor,
        Array(LightFX.CoreFractionStart, LightFX.CoreFractionEnd),
        Array(
          SimulationDefaults.UI.Colors.lightCenter,
          SimulationDefaults.UI.Colors.lightEdge,
        ),
      )
      g.setPaint(corePaint)
      g.fill(new Ellipse2D.Double(px, py, lightSize, lightSize))

      g.setColor(Color.ORANGE)
      g.setStroke(new BasicStroke(LightStroke))
      g.drawOval(px, py, lightSize, lightSize)

  end drawLight

  /**
   * Draws all robots in the environment.
   *
   * @param g
   *   Graphics context
   * @param env
   *   Environment containing robots
   * @param vp
   *   Viewport configuration
   * @param selectedId
   *   Optional ID of the selected robot
   */
  protected def drawDynamicEntities(
      g: Graphics2D,
      env: Environment,
      vp: Viewport,
      selectedId: Option[String] = None,
      includeId: Boolean = false,
  ): Unit =
    import io.github.srs.model.environment.dynamicEntities
    env.dynamicEntities.foreach(de => drawDynamicEntity(g, de, env, vp, selectedId, includeId))

  /**
   * Draws a single robot with body, direction indicator, and sensors.
   *
   * @param g
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param env
   *   Environment for sensor calculations
   * @param vp
   *   Viewport configuration
   * @param selectedId
   *   Optional ID of the selected robot
   */
  protected def drawDynamicEntity(
      g: Graphics2D,
      de: DynamicEntity,
      env: Environment,
      vp: Viewport,
      selectedId: Option[String],
      includeId: Boolean,
  ): Unit =
    val shape = de match
      case a: Agent => a.shape
      case r: Robot => r.shape

    shape match
      case ShapeType.Circle(radius) =>
        val isSelected = selectedId.contains(de.id.toString)
        drawDynamicEntityBody(g, de, radius, vp, isSelected, includeId)
        drawDynamicEntityDirection(g, de, radius, vp)
        drawSensorLines(g, de, radius, env, vp)

  /**
   * Draws the robot's circular body with gradient and border.
   *
   * @param g
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param radius
   *   Radius of the robot
   * @param vp
   *   Viewport configuration
   * @param isSelected
   *   True if this robot is currently selected
   */
  protected def drawDynamicEntityBody(
      g: Graphics2D,
      de: DynamicEntity,
      radius: Double,
      vp: Viewport,
      isSelected: Boolean,
      includeId: Boolean,
  ): Unit =
    import SimulationDefaults.DynamicEntity.Robot.{ NormalStroke, SelectionStroke }
    import SimulationDefaults.UI.{ Colors, Strokes }

    val x = vp.offsetX + (de.position.x - radius) * vp.scale
    val y = vp.offsetY + (de.position.y - radius) * vp.scale
    val d = 2 * radius * vp.scale

    val deShape = new Ellipse2D.Double(x, y, d, d)

    val colors =
      if isSelected then (Colors.robotSelected, Colors.robotSelectedDark, Colors.robotSelectedBorder)
      else (Colors.robotDefault, Colors.robotDefaultDark, Colors.robotDefaultBorder)

    val gradient = new RadialGradientPaint(
      (vp.offsetX + de.position.x * vp.scale).toFloat,
      (vp.offsetY + de.position.y * vp.scale).toFloat,
      (radius * vp.scale).toFloat,
      Array(RobotBody.GradientFractionStart, RobotBody.GradientFractionEnd),
      Array(colors._1, colors._2),
    )

    g.setPaint(gradient)
    g.fill(deShape)

    g.setColor(Colors.robotShadow)
    g.setStroke(new BasicStroke(Strokes.RobotShadowStroke))
    g.draw(deShape)

    g.setColor(colors._3)
    val strokeWidth = if isSelected then SelectionStroke else NormalStroke
    g.setStroke(new BasicStroke(strokeWidth))
    g.draw(deShape)

    if includeId then
      de match
        case a: DynamicEntity =>
          val id = a.id.toString.takeRight(2)
          val centerX = vp.offsetX + de.position.x * vp.scale
          val centerY = vp.offsetY + de.position.y * vp.scale

          val fontSize = (radius * vp.scale * 0.9).toInt.max(10)
          val font = new Font("SansSerif", Font.BOLD, fontSize)
          g.setFont(font)

          val fm = g.getFontMetrics
          val textWidth = fm.stringWidth(id)
          val textHeight = fm.getAscent

          g.setColor(Color.BLACK)
          g.drawString(id, (centerX - textWidth / 2 + 1).toInt, (centerY + textHeight / 4 + 1).toInt)

          g.setColor(Color.WHITE)
          g.drawString(id, (centerX - textWidth / 2).toInt, (centerY + textHeight / 4).toInt)
    end if

  end drawDynamicEntityBody

  /**
   * Draws an arrow indicating the robot's orientation.
   *
   * @param g
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param radius
   *   Radius of the robot
   * @param vp
   *   Viewport configuration
   */
  protected def drawDynamicEntityDirection(g: Graphics2D, de: DynamicEntity, radius: Double, vp: Viewport): Unit =
    import SimulationDefaults.DynamicEntity.Robot.*

    val cx = (vp.offsetX + de.position.x * vp.scale).toInt
    val cy = (vp.offsetY + de.position.y * vp.scale).toInt
    val angle = de.orientation.toRadians
    val length = radius * ArrowLengthFactor * vp.scale
    val width = math.max(MinArrowWidth.toDouble, radius * vp.scale * ArrowWidthFactor)

    val arrow = createArrowPolygon(cx, cy, angle, length, width)
    g.setColor(Color.BLACK)
    g.fillPolygon(arrow)

  /**
   * Draws sensor detection lines for a robot.
   *
   * @param g
   *   Graphics context
   * @param robot
   *   The robot whose sensors to draw
   * @param radius
   *   Radius of the robot
   * @param env
   *   Environment for sensor readings
   * @param vp
   *   Viewport configuration
   */
  protected def drawSensorLines(
      g: Graphics2D,
      de: DynamicEntity,
      radius: Double,
      env: Environment,
      vp: Viewport,
  ): Unit =
    val cx = (vp.offsetX + de.position.x * vp.scale).toInt
    val cy = (vp.offsetY + de.position.y * vp.scale).toInt
    val scaledRadius = radius * vp.scale

    val readings = de match
      case r: Robot => r.senseAll[Id](env).proximityReadings
      case a: Agent => a.senseAll[Id](env).proximityReadings

    readings.foreach { reading =>
      val sensor = reading.sensor
      val value = reading.value
      val sensorAngle = sensor.offset.toRadians + de.orientation.toRadians

      val startX = cx + (scaledRadius * math.cos(sensorAngle)).toInt
      val startY = cy + (scaledRadius * math.sin(sensorAngle)).toInt
      val sensorLength = DefaultRange * vp.scale
      val endX = startX + (sensorLength * math.cos(sensorAngle) * value).toInt
      val endY = startY + (sensorLength * math.sin(sensorAngle) * value).toInt

      g.setColor(Color.BLUE)
      g.setStroke(new BasicStroke(Sensors.LineStrokeWidth))
      g.drawLine(startX, startY, endX, endY)

      val dotSize = Sensors.DotSize
      g.fillOval(endX - dotSize / 2, endY - dotSize / 2, dotSize, dotSize)
    }

  end drawSensorLines

  /**
   * Creates a triangular polygon representing an arrow.
   *
   * @param cx
   *   Center X coordinate
   * @param cy
   *   Center Y coordinate
   * @param angle
   *   Direction angle in radians
   * @param length
   *   Length of the arrow
   * @param width
   *   Width of the arrow base
   * @return
   *   Polygon representing the arrow
   */
  private def createArrowPolygon(cx: Int, cy: Int, angle: Double, length: Double, width: Double): Polygon =
    val (cosA, sinA) = (math.cos(angle), math.sin(angle))
    val halfWidth = width / 2

    new Polygon(
      Array(
        (cx + cosA * length).toInt,
        (cx - sinA * halfWidth).toInt,
        (cx + sinA * halfWidth).toInt,
      ),
      Array(
        (cy + sinA * length).toInt,
        (cy + cosA * halfWidth).toInt,
        (cy - cosA * halfWidth).toInt,
      ),
      Arrow.TriangleVertices,
    )

end EnvironmentDrawing
