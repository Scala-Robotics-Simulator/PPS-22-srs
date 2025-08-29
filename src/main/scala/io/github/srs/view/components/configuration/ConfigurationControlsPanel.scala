package io.github.srs.view.components.configuration

import java.awt.FlowLayout
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import java.nio.file.NoSuchFileException

import scala.util.{ Failure, Success }
import scala.io.Source

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.Path
import io.github.srs.config.{ SimulationConfig, YamlConfigManager }
import io.github.srs.config.yaml.YamlManager
import io.github.srs.utils.loader.ResourceFileLister
import io.github.srs.config.ConfigError
import io.github.srs.model.environment.Environment

/**
 * ConfigurationControlsPanel handles the configuration loading, saving, and management controls. It provides buttons
 * for loading/saving configurations and a dropdown for selecting default configurations.
 *
 * @param onConfigLoaded
 *   callback triggered when a configuration is successfully loaded
 * @param onConfigSave
 *   callback triggered when user wants to save current configuration
 */
class ConfigurationControlsPanel(
    onConfigLoaded: SimulationConfig[Environment] => Unit,
    onConfigSave: () => Option[SimulationConfig[Environment]],
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
      YamlConfigManager[IO](path).load.attempt.unsafeRunAsync { result =>
        SwingUtilities.invokeLater { () =>
          val finalResult = result.flatten.flatten
          finalResult match
            case Left(_: NoSuchFileException) =>
              JOptionPane.showMessageDialog(
                this,
                s"File `${chooser.getSelectedFile.getAbsolutePath}` not found",
                "Error loading config",
                JOptionPane.ERROR_MESSAGE,
              )
            case Left(exception: Throwable) =>
              JOptionPane.showMessageDialog(
                this,
                s"Failed to load file: ${exception.getMessage}",
                "File Error",
                JOptionPane.ERROR_MESSAGE,
              )
            case Left(errors: Seq[ConfigError] @unchecked) =>
              JOptionPane.showMessageDialog(
                this,
                s"Parsing failed with error: ${errors.mkString("\n")}",
                "Error loading config",
                JOptionPane.ERROR_MESSAGE,
              )
            case Right(config) =>
              onConfigLoaded(config)
          end match
        }
      }

    end if

  end handleLoadConfiguration

  private def handleSaveConfiguration(): Unit =
    onConfigSave() match
      case None => ()
      case Some(config) =>
        val chooser = new JFileChooser()
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yml", "yaml"))
        val result = chooser.showSaveDialog(this)
        if result == JFileChooser.APPROVE_OPTION then
          val path = Path.fromNioPath(java.nio.file.Paths.get(chooser.getSelectedFile.toURI))
          YamlConfigManager[IO](path)
            .save(config)
            .attempt
            .unsafeRunAsync { result =>
              SwingUtilities.invokeLater { () =>
                val finalResult = result.flatMap(identity)
                finalResult match
                  case Left(exception) =>
                    JOptionPane.showMessageDialog(
                      this,
                      s"Failed to save file: ${exception.getMessage}",
                      "Save Error",
                      JOptionPane.ERROR_MESSAGE,
                    )
                  case Right(_) =>
                    JOptionPane.showMessageDialog(
                      this,
                      "Configuration saved successfully",
                      "Save configuration",
                      JOptionPane.INFORMATION_MESSAGE,
                    )
              }
            }
        end if

  private def handleConfigurationSelection(): Unit =
    configsComboBox.getSelectedItem match
      case configName: String =>
        loadResourceConfiguration(configName)
      case _ => ()

  private def loadResourceConfiguration(configName: String): Unit =
    val pathString = s"/$configsPath/${configName}.yml"
    Option(getClass.getResourceAsStream(pathString)) match
      case Some(resource) =>
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
      case None =>
        JOptionPane.showMessageDialog(
          this,
          s"Configuration file not found: $pathString",
          "Resource not found",
          JOptionPane.ERROR_MESSAGE,
        )
    end match

  end loadResourceConfiguration

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
    configsComboBox.getSelectedItem match
      case configName: String => Some(configName)
      case _ => None

end ConfigurationControlsPanel
