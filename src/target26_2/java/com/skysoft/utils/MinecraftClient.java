package com.skysoft.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftClient {
    private MinecraftClient() {
    }

    public static Screen screen() {
        return screen(Minecraft.getInstance());
    }

    public static Screen screen(Minecraft minecraft) {
        return minecraft.gui.screen();
    }

    public static void setScreen(Screen screen) {
        setScreen(Minecraft.getInstance(), screen);
    }

    public static void setScreen(Minecraft minecraft, Screen screen) {
        minecraft.gui.setScreen(screen);
    }

    public static boolean isGuiHidden(Minecraft minecraft) {
        return minecraft.gui.hud.isHidden();
    }

    public static ChatComponent chat(Minecraft minecraft) {
        return minecraft.gui.hud.getChat();
    }
}
