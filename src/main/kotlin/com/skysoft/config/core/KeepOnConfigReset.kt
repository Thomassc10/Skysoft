package com.skysoft.config.core

/**
 * Marks a ConfigResettable backing field that should keep its current value when reset() is called.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class KeepOnConfigReset
