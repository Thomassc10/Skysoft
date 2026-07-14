package com.skysoft.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int skysoft$getLeftPos();

    @Accessor("topPos")
    int skysoft$getTopPos();

    @Accessor("imageWidth")
    int skysoft$getImageWidth();

    @Accessor("imageHeight")
    int skysoft$getImageHeight();

    @Accessor("skipNextRelease")
    void skysoft$setSkipNextRelease(boolean skipNextRelease);

    @Accessor("hoveredSlot")
    Slot skysoft$getHoveredSlot();

    @Accessor("SLOT_HIGHLIGHT_BACK_SPRITE")
    static Identifier skysoft$getSlotHighlightBackSprite() {
        throw new AssertionError();
    }

    @Accessor("SLOT_HIGHLIGHT_FRONT_SPRITE")
    static Identifier skysoft$getSlotHighlightFrontSprite() {
        throw new AssertionError();
    }

    @Invoker("extractSlot")
    void skysoft$extractSlot(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY);

    @Invoker("slotClicked")
    void skysoft$slotClicked(Slot slot, int slotId, int button, ContainerInput action);
}
