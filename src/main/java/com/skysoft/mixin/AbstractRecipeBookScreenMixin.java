package com.skysoft.mixin;

import com.skysoft.features.misc.VanillaRecipeBookHider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public class AbstractRecipeBookScreenMixin {
    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateScreenPosition(II)I"
        )
    )
    private void skysoft$hideRecipeBookBeforePositioning(CallbackInfo ci) {
        if (skysoft$shouldHideInventoryRecipeBook()) {
            ((RecipeBookComponentAccessor) recipeBookComponent).skysoft$setVisible(false);
        }
    }

    @Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
    private void skysoft$skipRecipeBookButton(CallbackInfo ci) {
        if (skysoft$shouldHideInventoryRecipeBook()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "extractSlots",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;extractGhostRecipe(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Z)V"
        )
    )
    private void skysoft$suppressRecipeBookGhostRecipe(
        RecipeBookComponent<?> component,
        GuiGraphicsExtractor context,
        boolean biggerResultSlot
    ) {
        if (!skysoft$shouldHideInventoryRecipeBook()) {
            component.extractGhostRecipe(context, biggerResultSlot);
        }
    }

    @Unique
    private boolean skysoft$shouldHideInventoryRecipeBook() {
        return (Object) this instanceof InventoryScreen && VanillaRecipeBookHider.shouldHideInInventory();
    }
}
