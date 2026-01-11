package com.odtheking.odinaddon.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.odtheking.odinaddon.features.impl.skyblock.event.InputReleaseEvent;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    @Shadow
    private boolean isDown;
    @Shadow
    protected InputConstants.Key key;

    @Inject(
            method = "setDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preRelease(boolean bl, CallbackInfo ci) {
        if (!this.isDown || bl) return;
        if (new InputReleaseEvent(this.key).postAndCatch()) ci.cancel();
    }
}
