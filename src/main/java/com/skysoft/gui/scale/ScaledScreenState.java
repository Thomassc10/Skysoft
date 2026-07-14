package com.skysoft.gui.scale;

public interface ScaledScreenState {
    boolean skysoft$hasScaleDimensions();

    boolean skysoft$matchesScaleDimensions(int width, int height);

    void skysoft$rememberScaleDimensions(int width, int height);

    void skysoft$forgetScaleDimensions();
}
