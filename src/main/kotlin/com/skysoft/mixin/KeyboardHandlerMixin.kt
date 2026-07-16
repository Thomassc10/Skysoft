package com.skysoft.mixin

import com.skysoft.features.inventory.StorageOverlayController
import com.skysoft.features.inventory.itemlist.ItemListController
import com.skysoft.utils.MinecraftClient
import com.skysoft.utils.input.InputHandlingResult
import net.minecraft.client.KeyboardHandler
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.CharacterEvent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(KeyboardHandler::class)
open class KeyboardHandlerMixin {
    @Inject(method = ["charTyped"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftTypeStorageOverlay(window: Long, event: CharacterEvent, ci: CallbackInfo) {
        val screen = MinecraftClient.screen() as? AbstractContainerScreen<*> ?: return
        if (
            ItemListController.handleCharTyped(screen, event) == InputHandlingResult.CONSUMED ||
            StorageOverlayController.handleCharTyped(screen, event) == InputHandlingResult.CONSUMED
        ) {
            ci.cancel()
        }
    }
}
