package com.odtheking.odinaddon.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.itemUUID
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinaddon.features.impl.skyblock.ProtectItem
import net.minecraft.network.chat.Component


val protectItemCommand = Commodore("protectitem", "pi") {
    runs {
        val item = OdinMod.mc.player?.mainHandItem ?: return@runs modMessage("Please hold a valid item.")

        val protectedItem = ProtectItem.ProtectedItem(item.customName?.string, item.itemId, item.itemUUID)
        val itemList = ProtectItem.itemList
        itemList.find { protectedItem.uuid == it.uuid || protectedItem.sbID == it.sbID }?.let {
            itemList.remove(it)
            modMessage(
                Component.literal("Removed ")
                    .append(item.customName ?: Component.literal(it.sbID))
                    .append(Component.literal(" from protection whitelist."))
            )
            ModuleManager.saveConfigurations()
            return@runs
        }

        itemList.add(protectedItem)
        modMessage(
            Component.literal("Added ")
                .append(item.customName ?: Component.literal(protectedItem.sbID))
                .append(Component.literal(" to protection whitelist."))
        )
        ModuleManager.saveConfigurations()

    }
}