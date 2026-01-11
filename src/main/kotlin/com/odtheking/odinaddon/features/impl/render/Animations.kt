package com.odtheking.odinaddon.features.impl.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.exp

/**
 * Parts taken from:
 * [Odin](https://github.com/odtheking/Odin/)
 * [Floppa Client](https://github.com/FloppaCoding/FloppaClient)
 * [DulkirModFabric](https://github.com/inglettronald/DulkirMod-Fabric/)
 */
object Animations : Module (
    name = "Animations",
    description = "Changes animation style of item held in hand."
) {
    private val size by NumberSetting("Size", 1.0f, 0.1, 5, 0.05, desc = "Scales the size of your currently held item. Default: 0")
    private val x by NumberSetting("X", 0.0, -2.5, 2.5, 0.05, desc = "Moves held item (x-dir).")
    private val y by NumberSetting("Y", 0.0, -1.5, 2.5, 0.05, desc = "Moves held item (y-dir).")
    private val z by NumberSetting("Z", 0.0, -1.5, 2.5, 0.05, desc = "Moves held item (z-dir).")
    private val yaw by NumberSetting("Yaw", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    private val pitch by NumberSetting("Pitch", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    private val roll by NumberSetting("Roll", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    val stopEquipAnimation by BooleanSetting("Stop Equip", false, desc = "Stops re-equip animation")
    val ignoreHaste by BooleanSetting("Ignore Effects", false, desc = "Makes the chosen speed override haste modifiers.")
    val speed by NumberSetting("Speed Reduction", 6, 0, 100, 1, desc = "% speed reduction of the swing animation. 0 = no swing").withDependency { ignoreHaste }
    val stopFullSwing by BooleanSetting("Stop Arm Swing", false, desc = "Stops entire player arm from swinging.")

    fun hookItemTransform(matrix: PoseStack){
        if (!enabled) return

        matrix.apply {
            translate(x, y, z)

            mulPose(Axis.XP.rotationDegrees(pitch))
            mulPose(Axis.YP.rotationDegrees(yaw))
            mulPose(Axis.ZP.rotationDegrees(roll))
            scale(size, size, size)
        }
    }

}