package com.odtheking.odinaddon.features.impl.skyblock.event

import com.odtheking.odin.events.core.Event
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

open class WorldEvent : Event() {
    class AddBlock(val state: BlockState, val pos: BlockPos) : WorldEvent()

    class Load() : WorldEvent()

    class Unload() : WorldEvent()
}