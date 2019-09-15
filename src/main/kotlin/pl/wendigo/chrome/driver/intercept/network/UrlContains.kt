package pl.wendigo.chrome.driver.intercept.network

import pl.wendigo.chrome.api.network.Response
import pl.wendigo.chrome.driver.intercept.InterceptFilter

class UrlContains(private val url : String) : InterceptFilter<Response> {
    override fun accept(input: Response): Boolean {
        return input.url.contains(url)
    }

    override fun description(): String {
        return "UrlContains($url)"
    }
}

