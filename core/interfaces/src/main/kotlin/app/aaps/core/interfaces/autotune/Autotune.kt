package app.aaps.core.interfaces.autotune

interface Autotune {

    fun aapsAutotune(daysBack: Int, autoSwitch: Boolean, profileToTune: String = "")
    fun atLog(message: String)

    var lastRunSuccess: Boolean
    var calculationRunning: Boolean
}