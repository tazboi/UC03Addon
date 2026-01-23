package com.odtheking.odinaddon.features.impl.skyblock.event

import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents

object CustomEventDispatcher {
    init {
        onReceive<ClientboundPlayerPositionPacket> {
            if (!DungeonUtils.inDungeons || !DungeonUtils.inBoss) return@onReceive;
            EnterDungeonBossEvent(DungeonUtils.floor).postAndCatch()
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, world ->
            EntityWorldEvent.Join(entity, world).postAndCatch()
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, world ->
            EntityWorldEvent.Leave(entity, world).postAndCatch()
        }

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register { context, blockOutline ->
            !BlockOutlineEvent(context, blockOutline).postAndCatch()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            WorldEvent.Load().postAndCatch()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            WorldEvent.Unload().postAndCatch()
        }
    }
}