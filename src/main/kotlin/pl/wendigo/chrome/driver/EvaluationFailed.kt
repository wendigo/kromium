package pl.wendigo.chrome.driver

class EvaluationFailed constructor(override val message: String, throwable: Throwable?) : Exception(message, throwable) {
    constructor(message : String) : this(message, null)
}
