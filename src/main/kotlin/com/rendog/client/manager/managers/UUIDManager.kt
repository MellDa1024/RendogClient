package com.rendog.client.manager.managers

import com.rendog.client.capeapi.AbstractUUIDManager
import com.rendog.client.capeapi.PlayerProfile
import com.rendog.client.capeapi.UUIDUtils
import com.rendog.client.RendogMod
import com.rendog.client.manager.Manager
import com.rendog.client.util.Wrapper

object UUIDManager : AbstractUUIDManager(RendogMod.DIRECTORY + "uuid_cache.json", RendogMod.LOG, maxCacheSize = 1000), Manager {

    override fun getOrRequest(nameOrUUID: String): PlayerProfile? {
        return Wrapper.minecraft.connection?.playerInfoMap?.let { playerInfoMap ->
            val infoMap = ArrayList(playerInfoMap)
            val isUUID = UUIDUtils.isUUID(nameOrUUID)
            val withOutDashes = UUIDUtils.removeDashes(nameOrUUID)

            infoMap.find {
                isUUID && UUIDUtils.removeDashes(it.gameProfile.id.toString()).equals(withOutDashes, ignoreCase = true)
                    || !isUUID && it.gameProfile.name.equals(nameOrUUID, ignoreCase = true)
            }?.gameProfile?.let {
                PlayerProfile(it.id, it.name)
            }
        } ?: super.getOrRequest(nameOrUUID)
    }
}
