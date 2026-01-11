package com.odtheking.odinaddon.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.odtheking.odinaddon.features.impl.render.Animations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float partialTicks);

    @Shadow
    protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress);

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            )
    )
    private void preRender(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int j, CallbackInfo ci) {
        if (interactionHand == InteractionHand.MAIN_HAND) Animations.INSTANCE.hookItemTransform(poseStack);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getStopEquipAnimation()) {
            this.oMainHandHeight = 1.0f;
            this.mainHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandHeight = 1.0f;
        }
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
            )
    )
    private float overrideAttackStrengthScale(float originalValue) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getStopEquipAnimation() || Animations.INSTANCE.getStopFullSwing()) return 1f;
        return originalValue;
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void stopSwing(float swingProgress, float equipProgress, PoseStack poseStack, int swingTicks, HumanoidArm arm, CallbackInfo ci) {
        if (Animations.INSTANCE.getEnabled() && !Animations.INSTANCE.getStopFullSwing()) return;
        ci.cancel();
        this.applyItemArmTransform(poseStack, arm, equipProgress);
        this.applyItemArmAttackTransform(poseStack, arm, swingProgress);
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void forceInstantlyReplace(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getStopEquipAnimation()) cir.setReturnValue(true);
    }

}
