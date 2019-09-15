package pl.wendigo.chrome.driver.intercept.network

import pl.wendigo.chrome.api.network.Response
import pl.wendigo.chrome.driver.intercept.InterceptFilter

class ResponseMatches(private val lambda : Response.() -> Boolean) : InterceptFilter<Response> {
    override fun accept(input: Response): Boolean {
        return lambda.invoke(input)
    }

    override fun description(): String {
        return "ResponseMatches($lambda)"
    }
}