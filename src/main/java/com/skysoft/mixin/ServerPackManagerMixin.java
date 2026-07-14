package com.skysoft.mixin;

import com.google.common.hash.HashCode;
import com.skysoft.SkysoftMod;
import com.skysoft.features.misc.SkyBlockResourcePackManagerBridge;
import com.skysoft.features.misc.SkyBlockResourcePackRetention;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPackManager.class)
public abstract class ServerPackManagerMixin implements SkyBlockResourcePackManagerBridge {
    @Shadow
    @Final
    private PackLoadFeedback packLoadFeedback;

    @Shadow
    @Final
    private List<?> packs;

    @Shadow
    public abstract void popPack(UUID id);

    @Unique
    private @Nullable UUID skysoft$retainedPackId;

    @Unique
    private @Nullable HashCode skysoft$retainedPackHash;

    @Unique
    private boolean skysoft$retainedPackApplied;

    @Inject(method = "pushPack", at = @At("HEAD"), cancellable = true)
    private void skysoft$reuseRetainedPack(UUID id, URL url, @Nullable HashCode hash, CallbackInfo ci) {
        if (!SkyBlockResourcePackRetention.isEnabled()) {
            this.skysoft$clearRetainedPack();
            return;
        }
        if (!SkyBlockResourcePackRetention.isOfficialPackUrl(url)) {
            return;
        }
        if (!SkyBlockResourcePackRetention.canRetain(url, hash)) {
            SkysoftMod.Companion.getLOGGER().warn(
                "Cannot retain the official SkyBlock resource pack because it has no valid SHA-1 hash"
            );
            return;
        }
        if (this.skysoft$retainedPackApplied
            && id.equals(this.skysoft$retainedPackId)
            && hash.equals(this.skysoft$retainedPackHash)) {
            this.packLoadFeedback.reportUpdate(id, PackLoadFeedback.Update.ACCEPTED);
            this.packLoadFeedback.reportUpdate(id, PackLoadFeedback.Update.DOWNLOADED);
            this.packLoadFeedback.reportFinalResult(id, PackLoadFeedback.FinalResult.APPLIED);
            ci.cancel();
            return;
        }

        UUID previousPackId = this.skysoft$retainedPackApplied ? this.skysoft$retainedPackId : null;
        this.skysoft$retainedPackId = id;
        this.skysoft$retainedPackHash = hash;
        this.skysoft$retainedPackApplied = false;
        if (previousPackId != null && !previousPackId.equals(id)) {
            this.popPack(previousPackId);
        }
    }

    @Inject(method = "popPack", at = @At("HEAD"), cancellable = true)
    private void skysoft$keepPackLoaded(UUID id, CallbackInfo ci) {
        boolean retentionEnabled = SkyBlockResourcePackRetention.isEnabled();
        if (this.skysoft$retainedPackApplied && retentionEnabled && id.equals(this.skysoft$retainedPackId)) {
            ci.cancel();
        } else if (!retentionEnabled) {
            this.skysoft$clearRetainedPack();
        }
    }

    @Inject(method = "popAll", at = @At("HEAD"), cancellable = true)
    private void skysoft$keepPackLoaded(CallbackInfo ci) {
        boolean retentionEnabled = SkyBlockResourcePackRetention.isEnabled();
        if (!this.skysoft$retainedPackApplied || !retentionEnabled) {
            if (!retentionEnabled) {
                this.skysoft$clearRetainedPack();
            }
            return;
        }

        UUID retainedPackId = Objects.requireNonNull(this.skysoft$retainedPackId);
        this.packs.stream()
            .map(pack -> ((ServerPackDataAccessor) pack).skysoft$getId())
            .filter(id -> !id.equals(retainedPackId))
            .distinct()
            .forEach(this::popPack);
        ci.cancel();
    }

    @Override
    public void skysoft$markResourcePacksApplied(Collection<?> appliedPacks) {
        if (!SkyBlockResourcePackRetention.isEnabled() || this.skysoft$retainedPackId == null) {
            return;
        }
        this.skysoft$retainedPackApplied = appliedPacks.stream().anyMatch(pack -> {
            ServerPackDataAccessor data = (ServerPackDataAccessor) pack;
            return this.skysoft$retainedPackId.equals(data.skysoft$getId())
                && this.skysoft$retainedPackHash.equals(data.skysoft$getHash());
        });
    }

    @Unique
    private void skysoft$clearRetainedPack() {
        this.skysoft$retainedPackId = null;
        this.skysoft$retainedPackHash = null;
        this.skysoft$retainedPackApplied = false;
    }
}
