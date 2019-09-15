package pl.wendigo.chrome.driver.dom

interface CheckCondition {
    /**
     * Checks if condition holds
     */
    fun check() : Boolean

    /**
     * Describes what this condition represents
     */
    fun description() : String

    operator fun invoke() : Boolean = check()
}

enum class LoadingStatus {
    NAVIGATING,
    LOADED,
    IDLE
}

typealias EventTimestamp = Long