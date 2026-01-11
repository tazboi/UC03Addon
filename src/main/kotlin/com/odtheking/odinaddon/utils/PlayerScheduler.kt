package com.odtheking.odinaddon.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.handlers.TickTasks
import com.odtheking.odin.utils.modMessage
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket

object PlayerScheduler {
    var swapTasks = mutableMapOf<TickTask, Swap>()

    init {
        onSend<ServerboundSetCarriedItemPacket> {
            if (slot != (swapTasks.values.firstOrNull()?.slot ?: return@onSend)) flushSwaps()
        }

        onReceive<ClientboundSetHeldSlotPacket> {
            if (slot != (swapTasks.values.firstOrNull()?.slot ?: return@onReceive)) flushSwaps()
        }
    }

    fun scheduleSwap(index: Int, ticks: Int = 1, serverTick: Boolean = false) {
        if (ticks < 1) return
        val delay = (swapTasks.values.lastOrNull()?.delay ?: 1) + ticks //Always swap after last one scheduled

        lateinit var tickTask: TickTask //Copied from @TickTask
        tickTask = object : TickTask(delay, serverTick, {
            if (mc.screen == null && mc.player?.inventory?.selectedSlot != index) {
                mc.player?.inventory?.setSelectedSlot(index.coerceIn(0, 8)) //Setter has extra checks, not sure if kotlin is idiomatic enough to make sure those go through.
            }
            swapTasks.remove(tickTask)
            TickTasks.unregister(tickTask)
        }) {}
        swapTasks[tickTask] = Swap(delay, index)
    }

    fun flushSwaps() {
        swapTasks.keys.forEach {
            TickTasks.unregister(it)
        }
        swapTasks.clear()
    }

    data class Swap(val delay: Int, val slot: Int)

}