package kr.rendog.client.util.rendog

data class PlayerWeaponCD(
    var leftCD: Long, var rightCD: Long
)

data class WeaponCDData(
    val leftCD: Double, val rightCD: Double, val inVillage : Boolean
)

enum class CoolDownType { LEFT, RIGHT }