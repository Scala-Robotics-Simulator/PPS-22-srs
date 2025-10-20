package io.github.srs.view.rendering

import java.awt.*
import java.awt.image.BufferedImage

import io.github.srs.model.environment.Environment

/**
 * Headless renderer for generating environment images without Swing components. Suitable for server-side rendering via
 * gRPC.
 */
object EnvironmentRenderer extends EnvironmentDrawing:

  /**
   * Renders an environment to a BufferedImage.
   *
   * @param env
   *   Environment to render
   * @param width
   *   Image width in pixels
   * @param height
   *   Image height in pixels
   * @param backgroundColor
   *   Background color (default: white)
   * @return
   *   BufferedImage containing the rendered environment
   */
  private def render(
      env: Environment,
      width: Int,
      height: Int,
      backgroundColor: Color = Color.WHITE,
  ): BufferedImage =
    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()

    // Enable anti-aliasing for better quality
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)

    val vp = calculateViewport(env, width, height)

    // Draw background
    g.setColor(backgroundColor)
    g.fillRect(0, 0, width, height)

    // Draw layers
    drawGrid(g, env, vp)
    drawLabels(g, env, vp)
    drawStaticEntities(g, env, vp)
    drawRobots(g, env, vp)

    g.dispose()
    img

  end render

  /**
   * Renders environment and encodes as PNG bytes.
   *
   * @param env
   *   Environment to render
   * @param width
   *   Image width
   * @param height
   *   Image height
   * @return
   *   PNG-encoded byte array
   */
  def renderToPNG(env: Environment, width: Int, height: Int): Array[Byte] =
    val image = render(env, width, height)
    val baos = new java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(image, "png", baos)
    baos.toByteArray

end EnvironmentRenderer
