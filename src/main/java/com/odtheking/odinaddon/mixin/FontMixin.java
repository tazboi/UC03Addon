package com.odtheking.odinaddon.mixin;

import com.odtheking.odinaddon.features.impl.render.VisualWords;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {

    @ModifyVariable(
            method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private FormattedCharSequence modifyText(FormattedCharSequence original) {
        if (!VisualWords.INSTANCE.getEnabled()) return original;
        return VisualWords.INSTANCE.modify(original);
    }
}
