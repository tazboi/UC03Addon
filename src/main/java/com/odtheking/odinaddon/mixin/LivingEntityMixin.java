package com.odtheking.odinaddon.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odinaddon.features.impl.render.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow
    public boolean swinging;

    @ModifyExpressionValue(
            method = "updateSwingTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getCurrentSwingDuration()I"
            )
    )
    private int modifySwing(int original) {
        Player player = Minecraft.getInstance().player;
        if ((Object) this != player) return original;

        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getIgnoreHaste()) return Animations.INSTANCE.getSpeed();
        return original;

    }

    @Inject(
            method = "swing(Lnet/minecraft/world/InteractionHand;Z)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;swingTime:I",
                    opcode = (Opcodes.PUTFIELD),
                    ordinal = 0
            ),
            cancellable = true
    )
    private void preventClientSwing(InteractionHand interactionHand, boolean bl, CallbackInfo ci) {
                Player player = Minecraft.getInstance().player;
                if ((Object) this != player) return;
                if (!Animations.INSTANCE.getEnabled() || !Animations.INSTANCE.getStopSwingSpam()) return;

                if (this.swinging) ci.cancel();
    }

}
