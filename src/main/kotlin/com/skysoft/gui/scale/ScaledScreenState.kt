package com.skysoft.gui.scale

interface ScaledScreenState {
    fun skysoftHasScaleDimensions(): Boolean

    fun skysoftMatchesScaleDimensions(width: Int, height: Int): Boolean

    fun skysoftRememberScaleDimensions(width: Int, height: Int)

    fun skysoftForgetScaleDimensions()
}
