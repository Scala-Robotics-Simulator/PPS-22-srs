package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Locale
import java.awt.CardLayout

import io.github.srs.utils.chaining.Pipe.given
import io.github.srs.view.components.FieldSpec
import io.github.srs.view.components.FormPanel
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.config.ConfigResult
import io.github.srs.model.entity.ShapeType
import io.github.srs.model.entity.Orientation
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields
import io.github.srs.model.entity.dynamicentity.behavior.Policy

/**
 * EntityRow is a JPanel that represents a single entity in the configuration view. It allows users to select the type
 * of entity and fill in its properties.
 *
 * @param initialType
 *   the initial type of the entity
 * @param fieldSpecsByType
 *   a map where keys are entity types and values are sequences of FieldSpec defining the fields for each entity type
 * @param removeRow
 *   a function to call when the user wants to remove this row from the configuration
 */
class EntityRow(
    initialType: String,
    fieldSpecsByType: Map[String, Seq[FieldSpec]],
    removeRow: JPanel => Unit,
) extends JPanel(new BorderLayout):

  private val typeCombo = new JComboBox[String](fieldSpecsByType.keys.toArray)
  typeCombo.setSelectedItem(initialType)

  private val cardLayout = new CardLayout()
  private val propertiesContainer = new JPanel(cardLayout)

  private val propertyPanels: Map[String, FormPanel] = fieldSpecsByType.map { (entityType, fields) =>
    val panel = new FormPanel("", fields)
    propertiesContainer.add(panel, entityType)
    entityType -> panel
  }

  cardLayout.show(propertiesContainer, initialType)

  private val btnRemove = new JButton("X")
  btnRemove.setToolTipText("Remove this entity")

  private val rowPanel = new JPanel(new GridBagLayout)
  private val gbc = new GridBagConstraints()
  gbc.insets = new Insets(4, 4, 4, 4)
  gbc.fill = GridBagConstraints.HORIZONTAL

  gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0
  rowPanel.add(typeCombo, gbc)

  gbc.gridx = 1; gbc.weightx = 1.0
  rowPanel.add(propertiesContainer, gbc)

  gbc.gridx = 2; gbc.weightx = 0
  rowPanel.add(btnRemove, gbc)

  add(rowPanel, BorderLayout.CENTER)
  add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH)

  typeCombo.addActionListener(_ =>
    typeCombo.getSelectedItem match
      case selected: String => cardLayout.show(propertiesContainer, selected)
      case _ => (),
  )

  btnRemove.addActionListener(_ => removeRow(this))

  private def getCurrentPropertiesPanel: FormPanel =
    propertyPanels(getEntityType)

  private def getEntityType: String =
    typeCombo.getSelectedItem match
      case selected: String => selected
      case _ =>
        JOptionPane.showMessageDialog(
          this,
          "Invalid entity type selected.",
          "Error",
          JOptionPane.ERROR_MESSAGE,
        )
        ""

  def getEntity: ConfigResult[Entity] =
    getEntityType.toLowerCase(Locale.ENGLISH) match
      case RobotFields.Self => parseRobot()
      case ObstacleFields.Self => parseObstacle()
      case LightFields.Self => parseLight()

  def setValues(values: Map[String, String | Boolean]): Unit =
    getCurrentPropertiesPanel.setValues(values)

  private def parseRobot(): ConfigResult[Robot] =
    import Decoder.{ get, given }
    val map = getCurrentPropertiesPanel.getValues
    for
      x <- get[Double](EntityFields.X, map)
      y <- get[Double](EntityFields.Y, map)
      orientation <- get[Double](EntityFields.Orientation, map)
      radius <- get[Double](RobotFields.Radius, map)
      speed <- get[Double](RobotFields.Speed, map)
      prox <- get[Boolean](RobotFields.WithProximitySensors, map)
      light <- get[Boolean](RobotFields.WithLightSensors, map)
      behavior <- get[Policy](RobotFields.Behavior, map)
    yield robot
      .at((x, y))
      .withOrientation(Orientation(orientation))
      .withShape(ShapeType.Circle(radius))
      .withSpeed(speed)
      .withBehavior(behavior)
      |> (r => if prox then r.withProximitySensors else r)
      |> (r => if light then r.withLightSensors else r)

  end parseRobot

  private def parseObstacle(): ConfigResult[StaticEntity.Obstacle] =
    import Decoder.{ get, given }
    val map = getCurrentPropertiesPanel.getValues
    for
      x <- get[Double](EntityFields.X, map)
      y <- get[Double](EntityFields.Y, map)
      orientation <- get[Double](EntityFields.Orientation, map)
      width <- get[Double](ObstacleFields.Width, map)
      height <- get[Double](ObstacleFields.Height, map)
    yield obstacle at (x, y) withOrientation Orientation(orientation) withWidth width withHeight height

  private def parseLight(): ConfigResult[StaticEntity.Light] =
    import Decoder.{ get, given }
    val map = getCurrentPropertiesPanel.getValues
    for
      x <- get[Double](EntityFields.X, map)
      y <- get[Double](EntityFields.Y, map)
      orientation <- get[Double](EntityFields.Orientation, map)
      radius <- get[Double](LightFields.Radius, map)
      illumination <- get[Double](LightFields.IlluminationRadius, map)
      intensity <- get[Double](LightFields.Intensity, map)
      attenuation <- get[Double](LightFields.Attenuation, map)
    yield light
      .at(x, y)
      .withOrientation(Orientation(orientation))
      .withRadius(radius)
      .withIlluminationRadius(illumination)
      .withIntensity(intensity)
      .withAttenuation(attenuation)
end EntityRow
