package com.odtheking.odinaddon.mixin;


import com.mojang.blaze3d.platform.InputConstants;
import com.odtheking.odinaddon.features.impl.skyblock.event.KeyboardEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void beforePress(long l, int i, KeyEvent keyEvent, CallbackInfo ci) {
        if (Minecraft.getInstance().screen == null) return;

        InputConstants.Key keyType = InputConstants.getKey(keyEvent);
        if (new KeyboardEvent(keyType).postAndCatch()) ci.cancel();
    }
}
