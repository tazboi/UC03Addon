package com.odtheking.odinaddon.features.impl.skyblock.event

import com.odtheking.odin.events.core.CancellableEvent
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.renderer.state.BlockOutlineRenderState

class BlockOutlineEvent(val context: WorldRenderContext, val blockOutline: BlockOutlineRenderState) : CancellableEvent() {
}