package io.github.srs.model.lighting

/**
 * Lux is the SI unit of illuminance, measuring luminous flux per unit area.
 *
 * In this implementation, it's represented as a Double value where:
 *   - 0.0 represents complete darkness
 *   - 1.0 represents maximum illumination
 *   - Intermediate values represent partial illumination
 */
type Lux = Double
