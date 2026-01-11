package com.odtheking.odinaddon.mixin;


import com.mojang.blaze3d.platform.InputConstants;
import com.odtheking.odin.OdinMod;
import com.odtheking.odinaddon.features.impl.skyblock.event.MouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.odtheking.odin.utils.ChatUtilsKt.modMessage;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void prePress(long window, MouseButtonInfo mouseButtonInfo, int action, CallbackInfo ci) {
        if (Minecraft.getInstance().screen != null) return;
        fireEvent(mouseButtonInfo.button(), action, mouseButtonInfo.modifiers(), ci);
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void preScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (new MouseEvent.Scroll(horizontal, vertical).postAndCatch()) ci.cancel();
    }

    @Unique
    private static void fireEvent(int button, int action, int modifier, CallbackInfo ci) {
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        if (action == 1) {
            if (new MouseEvent.Click(key.getValue()).postAndCatch()) ci.cancel();
        } else {
            if (new MouseEvent.Release(key.getValue()).postAndCatch()) ci.cancel();
        }

    }
}
