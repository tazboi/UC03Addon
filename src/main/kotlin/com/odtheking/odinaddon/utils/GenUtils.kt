package com.odtheking.odinaddon.utils

import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomData

fun getRoomData(x: Number, z: Number): RoomData? =
    ScanUtils.coreToRoomData[
        ScanUtils.getCore(
            ScanUtils.getRoomCenter(
                x.toInt(), z.toInt()
            )
        )
    ]

