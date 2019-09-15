package pl.wendigo.chrome.driver.intercept.network

import io.reactivex.disposables.Disposable
import pl.wendigo.chrome.api.network.GetResponseBodyRequest
import pl.wendigo.chrome.api.network.RequestId
import pl.wendigo.chrome.api.network.Response
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.dom.CheckCondition
import pl.wendigo.chrome.driver.dom.Node
import pl.wendigo.chrome.driver.intercept.InterceptFilter
import pl.wendigo.chrome.driver.intercept.Interceptor
import pl.wendigo.chrome.driver.timedInfo
import java.util.Base64

/**
 * Captures network requests
 */
class Requests : Interceptor<Response, List<pl.wendigo.chrome.driver.intercept.network.Response>> {

    private lateinit var subscription : Disposable
    private val requests = mutableMapOf<Response, RequestId>()
    private var intercepted = listOf<pl.wendigo.chrome.driver.intercept.network.Response>()

    override fun start(parentNode: Node, context: SessionContext, filter: InterceptFilter<Response>) {
        subscription = context.protocol.Network.responseReceived().filter { event ->
            filter.accept(event.response)
        }.flatMapSingle { response ->
            context.protocol.Network.loadingFinished().filter {
                it.requestId == response.requestId
            }.firstOrError().map {
                Pair(response.response, it.requestId)
            }
        }
        .doOnSubscribe {
            context.logger.timedInfo(System.currentTimeMillis(), "Interceptor ${this.description()} started capturing responses matching ${filter.description()}")
        }
        .subscribe({ (response, requestId) ->
            requests[response] = requestId
            context.logger.timedInfo(System.currentTimeMillis(), "Interceptor ${this.description()} captured response{url=${response.url}} matching ${filter.description()}")
        }, { error ->
            context.logger.timedInfo(System.currentTimeMillis(), "Interceptor ${this.description()} got error ${error.message}")
        }, {
            context.logger.timedInfo(System.currentTimeMillis(), "Interceptor ${this.description()} has finished intercepting")
        })
    }

    override fun stop(parentNode: Node, context: SessionContext) {
        context.logger.timedInfo(System.currentTimeMillis(), "Stopping interceptor...")

        subscription.dispose()

        intercepted = requests.map {
            it.key.run {
                pl.wendigo.chrome.driver.intercept.network.Response(
                    url = url,
                    headers = headers,
                    statusCode = status.toInt(),
                    statusText = statusText,
                    body = getResponseBody(context, it.value)
                )
            }
        }.toList()
    }

    override fun intercepted() = intercepted

    private fun getResponseBody(context: SessionContext, id : RequestId) : String {

        context.logger.timedInfo(System.currentTimeMillis(), "Retrieving response for request $id")

        return context.protocol.Network.getResponseBody(GetResponseBodyRequest(id))
            .map {
                if (it.base64Encoded) {
                    String(Base64.getDecoder().decode(it.body))
                } else {
                    it.body
                }
            }.blockingGet()
    }

    fun capturedExactly(count : Int) : CheckCondition = object : CheckCondition {
        override fun check() = this@Requests.requests.size == count
        override fun description() = "Requests.capturedExactly($count) = ${this@Requests.requests.size}"
    }

    fun capturedAtLeast(count : Int) : CheckCondition = object : CheckCondition {
        override fun check() = this@Requests.requests.size >= count
        override fun description() = "Requests.capturedAtLeast($count) = ${this@Requests.requests.size}"
    }

    override fun description() = "Requests"
}