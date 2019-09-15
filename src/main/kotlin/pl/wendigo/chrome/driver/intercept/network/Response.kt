package pl.wendigo.chrome.driver.intercept.network

data class Response(
    val url : String,
    val statusCode : Int,
    val statusText : String,
    val headers : Map<String, Any>,
    val body : String
)