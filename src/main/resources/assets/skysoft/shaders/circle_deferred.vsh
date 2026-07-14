#version 150

// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

in vec3 Position;
in vec4 Color;
in vec4 RoundedParams0;
in vec4 RoundedParams1;

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

out vec4 tint;
out vec4 shapeData;
out vec4 arcData;

void main() {
    vec4 worldPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * worldPosition;
    tint = Color;
    shapeData = RoundedParams0;
    arcData = RoundedParams1;
}
