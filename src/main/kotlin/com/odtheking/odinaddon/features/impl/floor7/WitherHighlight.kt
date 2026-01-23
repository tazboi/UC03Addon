package com.odtheking.odinaddon.features.impl.floor7

import com.odtheking.odin.features.Module
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odinaddon.features.impl.skyblock.event.WorldEvent
import com.odtheking.odinaddon.utils.EntityCollection
import com.odtheking.odinaddon.utils.name
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.phys.AABB

object WitherHighlight : Module(
    name = "Wither Highlight",
    description = "Highlights Withers in Floor 7 Boss"
) {
    private val trueESP by BooleanSetting("True ESP", false, desc = "See withers through walls")
    private val color by ColorSetting(
        "Highlight color",
        Colors.WHITE.withAlpha(0.75f),
        true,
        desc = "The color of the highlight."
    )
    private val renderStyle by SelectorSetting(
        "Render Style",
        "Outline",
        listOf("Filled", "Outline", "Filled Outline"),
        desc = "Style of the box."
    )
//    private val goldorOnly by BooleanSetting(
//        "Goldor Only",
//        default = false,
//        desc = "Only Highlights Goldor's Position"
//    );

    private val IGNORED_WITHER_TICKS = 800;
    val witherEntities =
        EntityCollection({ it is WitherBoss && it.invulnerableTicks != IGNORED_WITHER_TICKS && !it.isInvisible })
    var lastWitherParticles: AABB? = null

    init {

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inDungeons || !DungeonUtils.inBoss) return@on;

            witherEntities.forEach { wither ->
                if (!wither.isAlive) return@forEach;

                drawStyledBox(
                    wither.renderBoundingBox,
                    color.multiplyAlpha(0.5f),
                    renderStyle,
                    !trueESP
                )
            }

            if (witherEntities.size == 0) {
                drawStyledBox(
                    lastWitherParticles ?: return@on, color.multiplyAlpha(0.5f),
                    renderStyle, depth = true
                )
            }
        }

        on<WorldEvent.Unload> {
            lastWitherParticles = null;
        }

//        onReceive<ClientboundLevelParticlesPacket> {
//            if (!DungeonUtils.inDungeons || !DungeonUtils.inBoss) return@onReceive;
//           // handleParticles(this);
//        }
    }

    // TODO: Find villager hurt particle, if it's same level as goldor
    // TODO: Find other wither particle types
//    private fun handleParticles(packet: ClientboundLevelParticlesPacket) {
//        modMessage(packet.name ?: return)
//    }

}