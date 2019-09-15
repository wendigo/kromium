package pl.wendigo.chrome.driver.intercept.screencast

data class Frame(
    val id : FrameId,
    val timestamp : Double,
    val data : String
)

typealias FrameId = Int