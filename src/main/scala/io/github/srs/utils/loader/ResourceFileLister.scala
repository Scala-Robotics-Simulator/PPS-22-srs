package io.github.srs.utils.loader

import java.nio.file.*

import scala.jdk.CollectionConverters.*
import scala.util.{ Try, Using }

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.TryPartial",
    "scalafix:DisableSyntax.null",
  ),
)
object ResourceFileLister:

  /**
   * Lists all files in the resources/configuration/default directory Works both in development (IDE) and when packaged
   * as JAR
   *
   * @param resourcePath
   *   the path to the resource directory, e.g., "configuration/default"
   */
  def listConfigurationFiles(resourcePath: String): Try[List[Path]] = Try:
    val resourceUrl = getClass.getClassLoader.getResource(resourcePath)

    if resourceUrl == null then throw new RuntimeException(s"Resource directory not found: $resourcePath")

    val uri = resourceUrl.toURI

    if uri.getScheme == "jar" then
      // Handle JAR file system
      Using(FileSystems.newFileSystem(uri, Map.empty[String, Any].asJava)) { fs =>
        val path = fs.getPath(resourcePath)
        Files.list(path).iterator().asScala.toList
      }.get
    else
      // Handle regular file system
      val path = Paths.get(uri)
      Files.list(path).iterator().asScala.toList

  /**
   * Lists only the filenames (without full paths)
   *
   * @param resourcePath
   *   the path to the resource directory, e.g., "configuration/default"
   */
  def listConfigurationFileNames(resourcePath: String): Try[List[String]] =
    listConfigurationFiles(resourcePath).map(_.map(_.getFileName.toString))

  /**
   * Filters for specific file extensions
   *
   * @param resourcePath
   *   the path to the resource directory, e.g., "configuration/default"
   *
   * @param extension
   *   the file extension to filter by (without the dot), e.g., "yml" or "yaml"
   */
  def listConfigurationFilesWithExtension(resourcePath: String, extension: String): Try[List[Path]] =
    listConfigurationFiles(resourcePath).map(
      _.filter(_.getFileName.toString.endsWith(s".$extension")),
    )
end ResourceFileLister
