package com.skysoft.config

import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor

object SkysoftMoulConfigGuis {
    fun <T : Config> createEditor(config: T): MoulConfigEditor<T> {
        val processor = MoulConfigProcessor(config)
        addProcessors(processor)
        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(config)
        return MoulConfigEditor(processor)
    }

    fun addProcessors(processor: MoulConfigProcessor<*>) {
        BuiltinMoulConfigGuis.addProcessors(processor)
        processor.registerConfigEditor(ConfigEditorUpdate::class.java) { option, _ ->
            SkysoftUpdateEditor(option)
        }
    }
}
