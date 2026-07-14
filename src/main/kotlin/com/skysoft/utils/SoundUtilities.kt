package com.skysoft.utils

import com.skysoft.SkysoftMod
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

object SoundUtilities {
    private val clickSound by lazy { createSound("ui.button.click", 1f) }

    fun playClickSound() {
        playSound(clickSound)
    }

    private fun createSound(name: String, pitch: Float, volume: Float = 50f): SoundInstance {
        val identifier = Identifier.parse(name.replace(Regex("[^a-z0-9/._-]"), ""))
        return SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(identifier), pitch, volume)
    }

    private fun playSound(sound: SoundInstance) {
        try {
            Minecraft.getInstance().soundManager.play(sound)
        } catch (e: IllegalArgumentException) {
            if (e.message?.startsWith("value already present:") == true) return
            SkysoftMod.LOGGER.warn("Failed to play sound {}", sound.identifier, e)
        }
    }
}
