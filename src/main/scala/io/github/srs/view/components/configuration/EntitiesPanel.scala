package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.BorderLayout
import java.awt.FlowLayout

import io.github.srs.view.components.FieldSpec
import io.github.srs.model.entity.Entity
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.collection.CustomSeq.sequence
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.StdProximitySensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.StdLightSensors
import io.github.srs.model.entity.ShapeType
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields

/**
 * EntitiesPanel is a JPanel that allows users to add and remove the simulation entities.
 *
 * @param fieldSpecsByType
 *   a map where keys are entity types and values are sequences of FieldSpec defining the fields for each entity type
 */
class EntitiesPanel(fieldSpecsByType: Map[String, Seq[FieldSpec]]) extends JPanel(new BorderLayout):
  private val entityListPanel = new JPanel()
  private val btnAddEntity = new JButton("+")

  initPanel()

  private def initPanel(): Unit =
    setBorder(BorderFactory.createTitledBorder("Entities"))
    val topBar = new JPanel(new FlowLayout(FlowLayout.LEFT))
    topBar.add(new JLabel("Add or remove entities:"))
    btnAddEntity.setToolTipText("Add a new entity")
    topBar.add(btnAddEntity)
    add(topBar, BorderLayout.NORTH)

    entityListPanel.setLayout(new BoxLayout(entityListPanel, BoxLayout.Y_AXIS))
    val scroll = new JScrollPane(entityListPanel)
    scroll.setBorder(BorderFactory.createEmptyBorder())
    add(scroll, BorderLayout.CENTER)

    btnAddEntity.addActionListener(_ =>
      val types = fieldSpecsByType.keys.toArray
      val selector = new JComboBox[String](types)
      val result = JOptionPane.showConfirmDialog(
        this,
        selector,
        "Select entity type",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE,
      )
      if result == JOptionPane.OK_OPTION then
        selector.getSelectedItem match
          case selected: String =>
            val row = new EntityRow(selected, fieldSpecsByType, removeEntityRow)
            entityListPanel.add(row)
            entityListPanel.revalidate()
            entityListPanel.repaint()
          case _ => (),
    )

  end initPanel

  private def removeEntityRow(row: JPanel): Unit =
    entityListPanel.remove(row)
    entityListPanel.revalidate()
    entityListPanel.repaint()

    /**
     * Retrieves the list of entities currently in the panel.
     *
     * @return
     *   a sequence of tuples where each tuple contains the entity type and a map of its values
     */
  def getEntities: ConfigResult[Seq[Entity]] =
    entityListPanel.getComponents.toIndexedSeq.collect { case r: EntityRow =>
      r.getEntity
    }.sequence

  /**
   * Lists the entities in the panel
   *
   * @param entities
   *   a list of entities to be displayed in the panel
   */
  def setEntities(entities: List[Entity]): Unit =
    import io.github.srs.model.entity.Point2D.*
    entityListPanel.removeAll()
    entities.foreach(e =>
      e match
        case robot: Robot =>
          val row = new EntityRow(RobotFields.Self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val robotMap = Map(
            EntityFields.X -> robot.position.x.toString,
            EntityFields.Y -> robot.position.y.toString,
            EntityFields.Orientation -> robot.orientation.degrees.toString,
            RobotFields.Radius -> robot.shape.radius.toString,
            RobotFields.Speed -> robot.actuators.collectFirst { case dwt: DifferentialWheelMotor =>
              dwt.left.speed.toString
            }.getOrElse(""),
            RobotFields.WithProximitySensors -> StdProximitySensors.forall(robot.sensors.contains),
            RobotFields.WithLightSensors -> StdLightSensors.forall(robot.sensors.contains),
            RobotFields.Behavior -> robot.behavior.toString(),
          )
          row.setValues(robotMap)
        case obs: StaticEntity.Obstacle =>
          val row = new EntityRow(ObstacleFields.Self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val shapeMap: Map[String, String | Boolean] = obs.shape match
            case ShapeType.Circle(_) => Map.empty[String, String | Boolean]
            case ShapeType.Rectangle(width, height) =>
              Map(
                ObstacleFields.Width -> width.toString,
                ObstacleFields.Height -> height.toString,
              )
          val obstacleMap = Map(
            EntityFields.X -> obs.position.x.toString,
            EntityFields.Y -> obs.position.y.toString,
            EntityFields.Orientation -> obs.orientation.degrees.toString,
          ) ++ shapeMap
          row.setValues(obstacleMap)
        case light: StaticEntity.Light =>
          val row = new EntityRow(LightFields.Self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val lightMap = Map(
            EntityFields.X -> light.position.x.toString,
            EntityFields.Y -> light.position.y.toString,
            EntityFields.Orientation -> light.orientation.degrees.toString,
            LightFields.Radius -> light.radius.toString,
            LightFields.IlluminationRadius -> light.illuminationRadius.toString,
            LightFields.Intensity -> light.intensity.toString,
            LightFields.Attenuation -> light.attenuation.toString,
          )
          row.setValues(lightMap)
      end match
      entityListPanel.revalidate()
      entityListPanel.repaint(),
    )
  end setEntities

end EntitiesPanel
