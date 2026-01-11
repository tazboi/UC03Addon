package com.odtheking.odinaddon.features.impl.skyblock

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.mixin.accessors.KeyMappingAccessor
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.itemUUID
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinaddon.features.impl.skyblock.event.InputReleaseEvent
import com.odtheking.odinaddon.utils.PlayerScheduler.scheduleSwap
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object ItemSwap: Module(
    name = "Item Swap",
    description = "Automatically swaps to a paired item after releasing use item key."
) {
    private val setItemPairKeybind by KeybindSetting("Link New Swap Pair", GLFW.GLFW_KEY_UNKNOWN, desc = "Links one item to another to swap to.")
    private val delay by NumberSetting("Delay", 3, 1, 20, desc = "Delay in ticks before swapping to the other item")
    private val swapMap = this.registerSetting(MapSetting("Item Swap Map", mutableMapOf<SwapItem, SwapItem>())).value

    private var previousItem: ItemStack? = null

    init {
        on<GuiEvent.KeyPress> {
            if (screen !is InventoryScreen || input.key != setItemPairKeybind.value) return@on
            val item = (screen as AbstractContainerScreenAccessor).hoveredSlot?.item ?: return@on

            cancel()

            previousItem?.let { secondItem ->
                if (item == secondItem) return@on modMessage("An item cannot be linked to itself.")

                swapMap[ SwapItem(secondItem.displayName?.string, secondItem.itemId, secondItem.itemUUID)] =
                    SwapItem(item.displayName?.string, item.itemId, item.itemUUID)

                ModuleManager.saveConfigurations()
                modMessage("${secondItem.displayName?.string} has been mapped to swap to  ${item.displayName?.string}")
            } ?: run {
                swapMap.keys.find { item.matchesSwapItem(it) }?.let {
                    swapMap.remove(it)
                    ModuleManager.saveConfigurations()
                    return@on modMessage("${item.displayName?.string} has been removed from Item Swaps")
                }

                previousItem = item
            }
        }

        on<GuiEvent.Close> {
            previousItem = null
        }

        on<InputReleaseEvent> {
            if (key != (mc.options.keyUse as KeyMappingAccessor).boundKey) return@on
            val held = mc.player?.mainHandItem ?: return@on
            val toSwap = swapMap.keys.firstOrNull { held.matchesSwapItem(it) } ?: return@on
            val exists = mc.player?.inventory?.find { it.matchesSwapItem(swapMap[toSwap] ?: return@on) }
            val index = mc.player?.inventory?.indexOf(exists).takeIf { it in 0..9 } ?: return@on

            scheduleSwap(index, 2)
        }
    }

    fun ItemStack.matchesSwapItem(swapItem: SwapItem) : Boolean {
        return this.itemUUID == swapItem.uuid || this.itemId == swapItem.sbID || this.displayName?.string == swapItem.name
    }
    data class SwapItem(val name: String?, val sbID: String, val uuid: String? = null)
}