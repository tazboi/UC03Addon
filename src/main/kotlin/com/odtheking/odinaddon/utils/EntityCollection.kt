package com.odtheking.odinaddon.utils

import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinaddon.features.impl.skyblock.event.EntityWorldEvent
import com.odtheking.odinaddon.features.impl.skyblock.event.WorldEvent

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.concurrent.CopyOnWriteArrayList

/*
* Original Author: @Litdab
* via Odin:
* com.odtheking.odin.utils.handlers.MobCache
 */
class EntityCollection(
    val predicate: (Entity) -> Boolean,
    val add: ((Entity) -> Entity?)? = null
) : CopyOnWriteArrayList<Entity>() {

    init {
        EntityCache.register(this)
    }

    fun addEntity(entity: Entity) {
        schedule(1) {
            predicate.takeIf { it(entity) } ?: return@schedule
            var toAdd = entity
            add?.let { toAdd = add.invoke(entity) ?: return@schedule }

            this.add(toAdd)
        }
    }

    fun removeEntity(entity: Entity) {
        this.remove(entity)
    }

}

object EntityCache {
    val cache = mutableListOf<EntityCollection>();

    init {
        on<EntityWorldEvent.Join> {
            for (coll in cache) {
                coll.addEntity(entity)
            }
        }

        on<EntityWorldEvent.Leave> {
            for (coll in cache) {
                coll.removeEntity(entity)
            }
        }

        on<WorldEvent.Unload> {
            for (coll in cache) {
                coll.clear()
            }
        }
    }

    fun register(collection: EntityCollection) {
        cache.add(collection);
    }

}