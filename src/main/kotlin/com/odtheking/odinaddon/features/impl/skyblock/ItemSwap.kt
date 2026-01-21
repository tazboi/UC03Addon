package com.odtheking.odinaddon.features.impl.skyblock

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
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
import com.odtheking.odinaddon.mixin.KeyMappingAccessor
import com.odtheking.odinaddon.utils.PlayerScheduler.scheduleSwap
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object ItemSwap: Module(
    name = "Item Swap",
    description = "Automatically swaps to a paired item after releasing use item key."
) {
    private val setItemPairKeybind by KeybindSetting("Link New Swap Pair", GLFW.GLFW_KEY_UNKNOWN, desc = "Links one item to another to swap to.")
    private val delay by NumberSetting("Delay", 3, 1, 20, desc = "Delay in ticks before swapping to the other item")
    private val swapMap = this.registerSetting(MapSetting("Item Swap Map", mutableMapOf<String, SwapItem>())).value

    private var previousItem: ItemStack? = null

    init {
        on<GuiEvent.KeyPress> {
            if (screen !is InventoryScreen || input.key != setItemPairKeybind.value) return@on
            val item = (screen as AbstractContainerScreenAccessor).hoveredSlot?.item.takeUnless { it!!.isEmpty } ?: return@on
            cancel()

            previousItem?.let { secondItem ->
                if (item == secondItem) return@on modMessage("An item cannot be linked to itself.")

                val itemInfo = secondItem.itemUUID.validOrNull() ?: secondItem.itemId.validOrNull() ?: return@on modMessage("Please use an item that has a Skyblock ID or UUID.")
                swapMap[itemInfo] =
                    SwapItem(item.displayName?.string, item.itemId, item.itemUUID)

                ModuleManager.saveConfigurations()
                modMessage(
                    Component.empty()
                        .append(secondItem.displayName)
                        .append(Component.literal(" has been mapped to swap to "))
                        .append(item.displayName)
                )

                previousItem = null
            } ?: run {
                swapMap.keys.find { item.itemId == it || item.itemUUID == it }?.let {
                    swapMap.remove(it)
                    ModuleManager.saveConfigurations()
                    return@on modMessage(
                        Component.empty()
                            .append(item.displayName)
                            .append(Component.literal(" has been removed from Item Swaps."))
                    )
                }

                previousItem = item
            }
        }

        on<GuiEvent.Close> {
            previousItem = null
        }

        on<InputReleaseEvent> {
            if (key != (mc.options.keyUse as KeyMappingAccessor).boundKey) return@on
            val held = mc.player?.mainHandItem?.let {
                it.itemUUID.validOrNull() ?: it.itemId.validOrNull()
            } ?: return@on
            val exists = mc.player?.inventory?.find { it.matchesSwapItem(swapMap[held] ?: return@on) }
            val index = mc.player?.inventory?.indexOf(exists).takeIf { it in 0 until 9 } ?: return@on

            scheduleSwap(index, delay)
        }
    }

    fun ItemStack.matchesSwapItem(swapItem: SwapItem) : Boolean {
        return this.itemUUID == swapItem.uuid || this.itemId == swapItem.sbID || this.displayName?.string == swapItem.name
    }

    fun String?.validOrNull() = takeUnless { it.isNullOrBlank() }

    data class SwapItem(val name: String?, val sbID: String, val uuid: String? = null)
}