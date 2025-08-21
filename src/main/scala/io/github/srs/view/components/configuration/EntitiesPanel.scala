package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.BorderLayout
import java.awt.FlowLayout

import io.github.srs.view.components.FieldSpec
import io.github.srs.model.entity.Entity
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.collection.CustomSeq.sequence
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.stdProximitySensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.stdLightSensors
import io.github.srs.model.entity.ShapeType
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
@SuppressWarnings(
  Array(
    "org.wartremover.warts.ToString",
  ),
)
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
        selector.getSelectedItem() match
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
    entityListPanel.getComponents.collect { case r: EntityRow =>
      r.getEntity
    }.sequence

  /**
   * Sets the entities in the panel
   *
   * @param entities
   *   a set of entities to be displayed in the panel
   */
  def setEntities(entities: Set[Entity]): Unit =
    import io.github.srs.model.entity.Point2D.*
    entityListPanel.removeAll()
    entities.foreach(e =>
      e match
        case robot: Robot =>
          val row = new EntityRow(RobotFields.self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val robotMap = Map(
            EntityFields.x -> robot.position.x.toString(),
            EntityFields.y -> robot.position.y.toString(),
            EntityFields.orientation -> robot.orientation.degrees.toString(),
            RobotFields.radius -> robot.shape.radius.toString(),
            RobotFields.speed -> robot.actuators.collectFirst { case dwt: DifferentialWheelMotor =>
              dwt.left.speed.toString()
            }.getOrElse(""),
            RobotFields.withProximitySensors -> stdProximitySensors.forall(robot.sensors.contains),
            RobotFields.withLightSensors -> stdLightSensors.forall(robot.sensors.contains),
            RobotFields.behavior -> robot.behavior.toString(),
          )
          row.setValues(robotMap)
        case obs: StaticEntity.Obstacle =>
          val row = new EntityRow(ObstacleFields.self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val shapeMap: Map[String, String | Boolean] = obs.shape match
            case ShapeType.Circle(_) => Map.empty[String, String | Boolean]
            case ShapeType.Rectangle(width, height) =>
              Map(
                ObstacleFields.width -> width.toString(),
                ObstacleFields.height -> height.toString(),
              )
          val obstacleMap = Map(
            EntityFields.x -> obs.position.x.toString(),
            EntityFields.y -> obs.position.y.toString(),
            EntityFields.orientation -> obs.orientation.degrees.toString(),
          ) ++ shapeMap
          row.setValues(obstacleMap)
        case light: StaticEntity.Light =>
          val row = new EntityRow(LightFields.self.capitalize, fieldSpecsByType, removeEntityRow)
          entityListPanel.add(row)
          val lightMap = Map(
            EntityFields.x -> light.position.x.toString(),
            EntityFields.y -> light.position.y.toString(),
            EntityFields.orientation -> light.orientation.degrees.toString(),
            LightFields.radius -> light.radius.toString(),
            LightFields.illuminationRadius -> light.illuminationRadius.toString(),
            LightFields.intensity -> light.intensity.toString(),
            LightFields.attenuation -> light.attenuation.toString(),
          )
          row.setValues(lightMap)
      end match
      entityListPanel.revalidate()
      entityListPanel.repaint(),
    )
  end setEntities

end EntitiesPanel
