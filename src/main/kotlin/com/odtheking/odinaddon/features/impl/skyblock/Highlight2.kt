package com.odtheking.odinaddon.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odinaddon.utils.EntityCollection
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

object Highlight2 : Module(
    name = "Better Highlight",
    description = "Highlights selected mobs"
) {
    val defaultColor by ColorSetting("Default Color", Colors.WHITE, true, desc = "The color of the highlight.")
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Filled", "Outline", "Filled Outline"), desc = "Style of the box.")
    private val trueESP by BooleanSetting("True ESP", false, desc = "See Highlight through blocks")
    var highlightMap = this.registerSetting(MapSetting("Highlight Map", mutableMapOf<String, Color>())).value
    private val OVERLOAD_REGEX = Regex("§f✯[\\s\\S]*?✯", RegexOption.DOT_MATCHES_ALL)

    val highlightCollection = EntityCollection(
        { entity ->
            highlightMap.keys.any { key ->
                val customName = entity.customName?.string ?: return@any false
                (customName.contains(key)) && !OVERLOAD_REGEX.matches(customName)
            }
        },
        {
            mc.level?.getEntities(it, it.boundingBox.move(0.0, -1.0, 0.0)) { isValid(it) }?.firstOrNull()
        }
    )

    init {
        on<RenderEvent.Extract> {
            highlightCollection.forEach { entity ->
                if (!entity.isAlive || entity.isRemoved) return@forEach
                val colorkey = highlightMap.keys.find { entity.customName?.string?.lowercase()?.contains(it) ?: false } ?: defaultColor
                drawStyledBox(
                    entity.renderBoundingBox,
                    highlightMap[colorkey] ?: defaultColor,
                    renderStyle,
                    !trueESP
                )
            }
        }
    }

    private fun isValid(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is WitherBoss -> false
            is Player -> entity.uuid.version() == 2 && entity != mc.player
            else -> entity is LivingEntity
        }
}