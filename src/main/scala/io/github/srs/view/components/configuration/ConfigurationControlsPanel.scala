package io.github.srs.view.components.configuration

import java.awt.FlowLayout
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

import scala.util.{ Failure, Success }
import scala.io.Source

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.Path
import io.github.srs.config.{ SimulationConfig, YamlConfigManager }
import io.github.srs.config.yaml.YamlManager
import io.github.srs.utils.loader.ResourceFileLister

/**
 * ConfigurationControlsPanel handles the configuration loading, saving, and management controls. It provides buttons
 * for loading/saving configurations and a dropdown for selecting default configurations.
 *
 * @param onConfigLoaded
 *   callback triggered when a configuration is successfully loaded
 * @param onConfigSave
 *   callback triggered when user wants to save current configuration
 * @param onConfigChanged
 *   callback triggered when user selects a different default configuration
 */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "scalafix:DisableSyntax.asInstanceOf",
    "scalafix:DisableSyntax.null",
  ),
)
class ConfigurationControlsPanel(
    onConfigLoaded: SimulationConfig => Unit,
    onConfigSave: () => Option[SimulationConfig],
    @annotation.unused onConfigChanged: String => Unit,
) extends JPanel(new FlowLayout(FlowLayout.CENTER)):

  private val configsPath = "configurations/default"
  private val loadButton = new JButton("Load")
  private val saveButton = new JButton("Save")
  private val configsComboBox = new JComboBox[String]()

  initPanel()

  private def initPanel(): Unit =
    loadDefaultConfigs()
    add(new JLabel("Configuration:"))
    add(configsComboBox)
    add(loadButton)
    add(saveButton)

    setupEventHandlers()

  private def setupEventHandlers(): Unit =
    loadButton.addActionListener(_ => handleLoadConfiguration())
    saveButton.addActionListener(_ => handleSaveConfiguration())
    configsComboBox.addActionListener(_ => handleConfigurationSelection())

  private def handleLoadConfiguration(): Unit =
    val chooser = new JFileChooser()
    chooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yml", "yaml"))
    val result = chooser.showOpenDialog(this)

    if result == JFileChooser.APPROVE_OPTION then
      val path = Path.fromNioPath(java.nio.file.Paths.get(chooser.getSelectedFile.toURI))
      val loadResult = YamlConfigManager[IO](path).load.unsafeRunSync()

      loadResult match
        case Left(errors) =>
          JOptionPane.showMessageDialog(
            this,
            s"Parsing failed with errors: ${errors.mkString(", ")}",
            "Error loading config",
            JOptionPane.ERROR_MESSAGE,
          )
        case Right(config) => onConfigLoaded(config)

  private def handleSaveConfiguration(): Unit =
    onConfigSave() match
      case Some(config) =>
        val chooser = new JFileChooser()
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yml", "yaml"))
        val result = chooser.showSaveDialog(this)

        if result == JFileChooser.APPROVE_OPTION then
          val path = Path.fromNioPath(java.nio.file.Paths.get(chooser.getSelectedFile.toURI))
          val saveEffect = for
            _ <- YamlConfigManager[IO](path).save(config)
            _ <- IO.pure(
              JOptionPane.showMessageDialog(
                this,
                "Configuration saved successfully",
                "Save configuration",
                JOptionPane.INFORMATION_MESSAGE,
              ),
            )
          yield ()
          saveEffect.unsafeRunAsync(_ => ())
      case None => ()

  private def handleConfigurationSelection(): Unit =
    Option(configsComboBox.getSelectedItem) match
      case Some(item) =>
        val configName = item.asInstanceOf[String]
        loadResourceConfiguration(configName)
      case None => ()

  private def loadResourceConfiguration(configName: String): Unit =
    val pathString = s"/${configsPath}/${configName}.yml"
    val resource = getClass.getResourceAsStream(pathString)

    if resource != null then
      val content = Source.fromInputStream(resource, "UTF-8").getLines().mkString("\n")
      val parseEffect =
        for res <- YamlManager.parse[IO](content)
        yield res match
          case Left(errors) =>
            JOptionPane.showMessageDialog(
              this,
              s"Parsing default configuration failed with errors: ${errors.mkString(", ")}",
              "Error loading default config",
              JOptionPane.ERROR_MESSAGE,
            )
          case Right(config) => onConfigLoaded(config)

      parseEffect.unsafeRunAsync(_ => ())

  private def loadDefaultConfigs(): Unit =
    ResourceFileLister.listConfigurationFilesWithExtension(configsPath, "yml") match
      case Failure(exception) =>
        println(s"Unable to load configurations: ${exception.getMessage}")
      case Success(files) =>
        val configNames = files.map(_.getFileName.toString.replaceAll("\\.[^.]*$", ""))
        configNames.foreach(configsComboBox.addItem)

        // Load first configuration by default
        configNames.headOption.foreach(loadResourceConfiguration)

  /**
   * Gets the currently selected configuration name.
   */
  def getSelectedConfiguration: Option[String] =
    Option(configsComboBox.getSelectedItem).map(_.asInstanceOf[String])

end ConfigurationControlsPanel
