package kr.rendog.client.manager.managers

import kr.rendog.client.RendogMod
import kr.rendog.client.capeapi.AbstractUUIDManager
import kr.rendog.client.capeapi.PlayerProfile
import kr.rendog.client.capeapi.UUIDUtils
import kr.rendog.client.manager.Manager
import kr.rendog.client.util.FolderUtils
import kr.rendog.client.util.Wrapper

object UUIDManager : AbstractUUIDManager(FolderUtils.rendogFolder + "uuid_cache.json", RendogMod.LOG, maxCacheSize = 1000), Manager {

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
