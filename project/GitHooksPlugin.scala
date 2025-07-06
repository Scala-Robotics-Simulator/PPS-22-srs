import sbt._
import Keys._
import scala.sys.process._

object GitHooksPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val configGitHooks = taskKey[Unit]("Configure git to use hooks from .githooks directory")
  }

  import autoImport._

  override def buildSettings = Seq(
    configGitHooks := {
      val result = "git config core.hooksPath .githooks".!
      if (result == 0) {
        println("Successfully configured git hooks path")
      } else {
        println("Failed to configure git hooks path")
      }
    },

    onLoad in Global := {
      val previous = (onLoad in Global).value
      previous andThen { state =>
        val isCI = sys.env.get("CI").contains("true")
        if (!isCI) {
          // Use safer command injection instead of direct task execution
          "configGitHooks" :: state
        } else {
          state
        }
      }
    }
    )
}
