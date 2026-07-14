package com.skysoft.config.features.pets.display

import com.skysoft.config.core.ConfigResettable

interface ScalableOverlayConfig : ConfigResettable {
    val scalar: Float get() = 1.0f
    override val resetConstructorScalar: Float? get() = scalar
}
