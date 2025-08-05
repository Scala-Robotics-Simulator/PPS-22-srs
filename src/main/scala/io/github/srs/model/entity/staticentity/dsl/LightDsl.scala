package io.github.srs.model.entity.staticentity.dsl

import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.positive

/**
 * The DSL for creating and configuring lights in the simulation.
 */
object LightDsl:

  /**
   * Creates a new light with default properties.
   * @return
   *   A new instance of [[Light]] with default values.
   */
  def light: Light = Light()

  /**
   * Provides an extension method for the Light class to allow for a more fluent DSL.
   */
  extension (light: Light)
    /**
     * Sets the position of the light.
     * @param pos
     *   the position of the light.
     * @return
     *   The updated light with the specified position.
     */
    infix def at(pos: Point2D): Light = light.copy(pos = pos)

    /**
     * Sets the orientation of the light.
     * @param orientation
     *   the orientation of the light.
     * @return
     *   The updated light with the specified orientation.
     */
    infix def withOrientation(orientation: Orientation): Light = light.copy(orient = orientation)

    /**
     * Sets the radius of the light, which defines the dimension of the light bulb.
     * @param radius
     *   the radius of the light bulb.
     * @return
     *   The updated light with the specified radius.
     */
    infix def withRadius(radius: Double): Light = light.copy(radius = radius)

    /**
     * Sets the illumination radius of the light, which defines how far the light can illuminate. This is different from
     * the radius of the light bulb, as it defines the effective range of the light's illumination.
     * @param radius
     *   the illumination radius of the light.
     * @return
     *   The updated light with the specified illumination radius.
     */
    infix def withIlluminationRadius(radius: Double): Light = light.copy(illuminationRadius = radius)

    /**
     * Sets the intensity of the light
     * @param intensity
     *   the intensity of the light.
     * @return
     *   The updated light with the specified intensity.
     */
    infix def withIntensity(intensity: Double): Light = light.copy(intensity = intensity)

    /**
     * Sets the attenuation of the light
     * @param attenuation
     *   the attenuation factor of the light.
     * @return
     *   The updated light with the specified attenuation.
     */
    infix def withAttenuation(attenuation: Double): Light = light.copy(attenuation = attenuation)

    infix def validate: Validation[Light] =
      for
        r <- positive("radius", light.radius)
        ir <- positive("illumination radius", light.illuminationRadius)
        i <- positive("intensity", light.intensity)
        a <- positive("attenuation", light.attenuation)
      yield light.copy(radius = r, illuminationRadius = ir, intensity = i, attenuation = a)
  end extension
end LightDsl
