package com.skysoft.mixin;

import com.skysoft.events.sound.ClientSoundEvent;
import com.skysoft.events.sound.ClientSoundEvents;
import com.skysoft.utils.WorldVec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class SoundPacketMixin {
    @Inject(
        method = "handleSoundEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread("
                + "Lnet/minecraft/network/protocol/Packet;"
                + "Lnet/minecraft/network/PacketListener;"
                + "Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER
        )
    )
    private void skysoft$postReceiveSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        ClientSoundEvents.INSTANCE.getEVENT().invoker().onReceiveSound(
            new ClientSoundEvent(
                packet.getSound().value(),
                packet.getSource(),
                new WorldVec(packet.getX(), packet.getY(), packet.getZ()),
                null,
                packet.getVolume(),
                packet.getPitch(),
                packet.getSeed()
            )
        );
    }

    @Inject(
        method = "handleSoundEntityEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread("
                + "Lnet/minecraft/network/protocol/Packet;"
                + "Lnet/minecraft/network/PacketListener;"
                + "Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER
        )
    )
    private void skysoft$postReceiveEntitySoundEvent(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(packet.getId());
        ClientSoundEvents.INSTANCE.getEVENT().invoker().onReceiveSound(
            new ClientSoundEvent(
                packet.getSound().value(),
                packet.getSource(),
                entity == null ? null : new WorldVec(entity.getX(), entity.getY(), entity.getZ()),
                packet.getId(),
                packet.getVolume(),
                packet.getPitch(),
                packet.getSeed()
            )
        );
    }
}
