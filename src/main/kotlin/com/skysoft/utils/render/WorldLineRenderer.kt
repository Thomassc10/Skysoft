package com.skysoft.utils.render

import com.skysoft.utils.WorldVec
import java.awt.Color

object WorldLineRenderer {
    fun drawToCrosshair(
        context: SkysoftRenderContext,
        location: WorldVec,
        color: Color,
        lineWidth: Int = DEFAULT_LINE_WIDTH,
        depth: Boolean = false,
    ) {
        context.drawLineToCrosshair(location, color, lineWidth, depth)
    }

    private const val DEFAULT_LINE_WIDTH = 3
}
