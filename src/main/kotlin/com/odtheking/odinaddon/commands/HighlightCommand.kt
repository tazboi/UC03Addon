package com.odtheking.odinaddon.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinaddon.features.impl.skyblock.Highlight2
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import java.lang.Exception

val highlightCommand = Commodore("highlight", "hl") {

    literal("add", "a").runs { input: GreedyString ->
        val trimmed = input.string.trim()

        if (trimmed.isEmpty()) return@runs modMessage("Invalid format. Use: /highlight add <mobname>")
        if (Highlight2.highlightMap.containsKey(trimmed)) return@runs modMessage("$trimmed already exists in the highlight list.")

        val color = Highlight2.defaultColor
        Highlight2.highlightMap[trimmed] = color
        modMessage("${trimmed} added to highlight list")
        ModuleManager.saveConfigurations()
    }


    literal("remove", "r", "d", "del").runs { name: GreedyString ->
        val lowerTrimmed = name.string.trim().lowercase()
        if (lowerTrimmed.isEmpty()) return@runs modMessage("Name cannot be empty.")

        val toRemove =
            Highlight2.highlightMap.keys.find { it.lowercase().contains(lowerTrimmed) } ?: return@runs modMessage(
                "$name was not found in the highlight list."
            )
        Highlight2.highlightMap.remove(toRemove)
        modMessage("$toRemove was successfully removed from the highlight list.")
        ModuleManager.saveConfigurations()
    }

    literal("list", "l").runs {
        if (Highlight2.highlightMap.isEmpty()) return@runs modMessage("Highlight list is empty.")

        Highlight2.highlightMap.forEach { (mob, color) ->
            val line = Component.literal("- $mob ")
                .append(
                    Component.literal("§c[Remove]")
                        .withStyle {
                            it.withHoverEvent(
                                HoverEvent.ShowText(
                                    Component.literal("§eClick to remove §f$mob§e from highlights.")
                                )
                            ).withClickEvent(
                                ClickEvent.RunCommand("/hl r $mob")
                            )
                        }
                )
            modMessage(line)
        }
    }
}
