package com.odtheking.odinaddon.features.impl.skyblock

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.mixin.accessors.KeyMappingAccessor
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinaddon.features.impl.skyblock.event.KeyboardEvent
import com.odtheking.odinaddon.features.impl.skyblock.event.MouseEvent
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object Click : Module(
    name = "Auto Clicker",
    description = "Autoclicks left/right click"
) {
    private val lcEnabled by BooleanSetting("Left Click", false, desc = "Toggle Left Click AC")
    private val lcHold by BooleanSetting("Left Click Hold", false, desc = "Hold to Use AC").withDependency { lcEnabled }
    private val lcUse by KeybindSetting(
        "Use LC",
        GLFW.GLFW_KEY_UNKNOWN,
        desc = "Keybind to Use"
    ).withDependency { lcEnabled }
    private val termOnly by BooleanSetting(
        "Terminator Only",
        false,
        desc = "Left clicks automatically only while using terminator."
    ).withDependency { lcEnabled }
    private val leftCps by NumberSetting(
        "Left Clicks per Second",
        5,
        min = 1,
        max = 15,
        increment = 1,
        desc = "Number of clicks per second"
    ).withDependency { lcEnabled }

    private val rcEnabled by BooleanSetting("Right Click", false, desc = "Toggle Right Click AC")
    private val rcHold by BooleanSetting(
        "Right Click Hold",
        false,
        desc = "Hold to Use AC"
    ).withDependency { rcEnabled }
    private val rcUse by KeybindSetting(
        "Use RC",
        GLFW.GLFW_KEY_UNKNOWN,
        desc = "Keybind to Use"
    ).withDependency { rcEnabled }
    private val rightCps by NumberSetting(
        "Right Clicks per Second",
        5,
        min = 1,
        max = 15,
        increment = 1,
        desc = "Number of clicks per second"
    ).withDependency { rcEnabled }


    private var nextLeftClick = .0
    private var nextRightClick = .0

    private var lastLCState = false;
    private var lastRCState = false;

    init {
        nextRightClick = 0.0
        nextLeftClick = 0.0
        on<RenderEvent.Last> {
            val nowMillis = System.currentTimeMillis()
            if (mc.screen != null || (!rcEnabled && !lcEnabled)) return@on;
            if (rcEnabled) tryRightClick(nowMillis)
            if (lcEnabled) tryLeftClick(nowMillis, termOnly)
        }

        on<MouseEvent.Click> {
            handleInputEvent(this, button, termOnly);
        }

        on<KeyboardEvent> {
            handleInputEvent(this, button.value, termOnly);
        }
    }

    private fun tryLeftClick(now: Long, termOnly: Boolean = false) {
        if (!termOnly) {
            if ((!lcUse.isDown && lcHold) || (!lcHold && !lastLCState)) return;
        } else if (!(mc.player?.mainHandItem?.itemId == "TERMINATOR" && (mc.options.keyUse as KeyMappingAccessor).boundKey.isDown)) return

        if (now < nextLeftClick) return;

        val key = (mc.options.keyAttack as KeyMappingAccessor).boundKey;
        nextLeftClick =
            now + ((1000 / leftCps) + ((Math.random() - .5) * 60.0)) + 4 //System.currentMillis() + (1s/cps + random 0-1s - 0.5s * 60s) + 4ms
        KeyMapping.click(key);

    }

    private fun tryRightClick(now: Long) {
        if ((!rcUse.isDown && rcHold) || (!rcHold && !lastRCState)) return;
        if (now < nextRightClick) return;

        val key = (mc.options.keyUse as KeyMappingAccessor).boundKey;
        nextRightClick = now + ((1000 / rightCps) + ((Math.random() - .5) * 60.0)) + 4
        KeyMapping.click(key);
    }

    private fun handleInputEvent(event: CancellableEvent, button: Int, termOnly: Boolean) {
        val left = (mc.options.keyAttack as KeyMappingAccessor).boundKey.value
        val right = (mc.options.keyUse as KeyMappingAccessor).boundKey.value
        val shouldTerm = mc.player?.mainHandItem?.itemId == "TERMINATOR" && (mc.options.keyUse as KeyMappingAccessor).boundKey.isDown

        val lcActive = if (termOnly) shouldTerm else lcEnabled && ((lcHold && lcUse.isDown) || (!lcHold && lastLCState))
        val rcActive = rcEnabled && ((rcHold && rcUse.isDown) || (!rcHold && lastRCState))

        when (button) {
            left -> if (lcActive) event.cancel()
            right -> if (rcActive) event.cancel()
        }

        if (!lcHold && lcEnabled && button == lcUse.value) lastLCState = !lastLCState
        if (!rcHold && rcEnabled && button == rcUse.value) lastRCState = !lastRCState
    }


    val InputConstants.Key.isDown: Boolean
        get() {
            val v = this.value;
            val mouse = if (v in 0..7) GLFW.glfwGetMouseButton(mc.window.window, v) else -1;
            val key =
                if (v >= GLFW.GLFW_KEY_SPACE && v <= GLFW.GLFW_KEY_LAST) GLFW.glfwGetKey(mc.window.window, v) else -1;

            return 1.equalsOneOf(mouse, key);
        }

}