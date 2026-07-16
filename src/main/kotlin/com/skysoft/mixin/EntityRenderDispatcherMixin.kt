package com.skysoft.mixin

import com.skysoft.features.event.diana.DianaRareMobEntityMatcher
import com.skysoft.features.misc.DeadEntityHider
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.world.entity.Entity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(EntityRenderDispatcher::class)
abstract class EntityRenderDispatcherMixin {
    @Inject(method = ["shouldRender"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftHideEntity(
        entity: Entity,
        frustum: Frustum,
        cameraX: Double,
        cameraY: Double,
        cameraZ: Double,
        cir: CallbackInfoReturnable<Boolean>,
    ) {
        if (DeadEntityHider.shouldHide(entity) || DianaRareMobEntityMatcher.shouldHideBuggedEntity(entity)) {
            cir.returnValue = false
        }
    }
}
