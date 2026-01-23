package com.odtheking.odinaddon.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odinaddon.features.impl.skyblock.event.WorldEvent
import com.odtheking.odinaddon.utils.EntityCollection
import com.odtheking.odinaddon.utils.getRoomData
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.level.block.TrappedChestBlock
import net.minecraft.world.phys.AABB

object MimicChestHighlight : Module(
    name = "Mimic Chest Highlight",
    description = "Highlights Mimics and Mimic Chests in Dungeons"
) {
    private val trueESP by BooleanSetting("True ESP", false, desc = "See mimics and mimic chests through walls")
    private val chestColor by ColorSetting(
        "Chest color",
        Colors.WHITE.withAlpha(0.75f),
        true,
        desc = "The color of the chest highlight."
    )
    private val hlColor by ColorSetting(
        "Highlight color",
        Colors.WHITE.withAlpha(0.75f),
        true,
        desc = "The color of the mob highlight."
    )
    private val renderStyle by SelectorSetting(
        "Render Style",
        "Outline",
        listOf("Filled", "Outline", "Filled Outline"),
        desc = "Style of the box."
    )

    var currBlock: BlockPos? = null
    var roomName: String? = null
    val mimic = EntityCollection({ it is Zombie && it.isBaby })

    init {
        on<RenderEvent.Extract> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@on
            currBlock?.let {
                if (roomName != DungeonUtils.currentRoom?.data?.name) return@let
                drawStyledBox(
                    AABB(it),
                    chestColor.multiplyAlpha(0.5f),
                    renderStyle,
                    !trueESP
                )
            }

            mimic.firstOrNull()?.let {
                drawStyledBox(
                    it.renderBoundingBox,
                    hlColor.multiplyAlpha(0.5f),
                    renderStyle,
                    !trueESP
                )
            }
        }

        TickTask(20) {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss || roomName != null) return@TickTask
            currBlock?.let {
                roomName = getRoomData(it.x, it.z)?.name ?: return@TickTask
            }
        }

        on<WorldEvent.AddBlock> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@on
            currBlock ?: run {
                if (state.block is TrappedChestBlock) {
                    currBlock = pos
                }
            }
        }

        on<BlockUpdateEvent> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@on
            currBlock?.let {
                if (old.block is TrappedChestBlock && updated.isAir) currBlock = null
            }
        }

        on<WorldEvent.Unload> {
            currBlock = null
            roomName = null
        }
    }

}