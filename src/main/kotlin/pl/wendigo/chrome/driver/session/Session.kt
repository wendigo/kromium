package pl.wendigo.chrome.driver.session

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.DevToolsProtocol
import pl.wendigo.chrome.api.network.GetCertificateRequest
import pl.wendigo.chrome.api.network.SetBlockedURLsRequest
import pl.wendigo.chrome.api.network.SetUserAgentOverrideRequest
import pl.wendigo.chrome.api.page.FrameId
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.dom.Document
import pl.wendigo.chrome.driver.timedInfo
import pl.wendigo.chrome.driver.timedWarn
import pl.wendigo.chrome.protocol.inspector.InspectablePage
import java.io.Closeable
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Session represents single, debuggable chrome session.
 */
open class Session constructor(
        private val page : InspectablePage,
        private val protocol: DevToolsProtocol,
        private val browser: Browser,
        protected val logger : Logger = LoggerFactory.getLogger("session-${page.id}")
) : Closeable {

    private val context : SessionContext = SessionContext(logger = logger, protocol = protocol, session = this@Session)
    private val document : Document = Document(FrameId(), context)
    protected val closed : AtomicBoolean = AtomicBoolean(false)

    /**
     * Overrides user agent.
     */
    fun userAgent(userAgent : String) : Session {
        context.enableNetwork.flatMap {
            protocol.Network.setUserAgentOverride(SetUserAgentOverrideRequest(userAgent))
        }.blockingGet()

        return this
    }

    /**
     * Disallows loading urls from list (supports wildcard)
     */
    fun blockUrls(vararg urls : String) : Session {
        context.enableNetwork.flatMap {
            protocol.Network.setBlockedURLs(SetBlockedURLsRequest(urls.asList()))
        }.blockingGet()

        return this
    }

    /**
     * Returns DER encoded certificate list.
     */
    fun certificates(origin : String) : List<X509Certificate> {
        val list = context.enableNetwork.flatMap {
            protocol.Network.getCertificate(GetCertificateRequest(origin))
        }.blockingGet().tableNames

        val certificates = mutableListOf<X509Certificate>()

        val cf = CertificateFactory.getInstance("X.509")

        list.forEach {
            val certs = cf.generateCertificates(
                    """-----BEGIN CERTIFICATE-----
                        $it
                        -----END CERTIFICATE-----"""
                            .byteInputStream()
            )

            certs.forEach {
                certificates.add(it as X509Certificate)
            }
        }

        return certificates.toList()
    }

    /**
     * Navigates to URL
     */
    fun navigate(url : String) = document.navigate(url)

    /**
     * Returns new, not navigated document.
     */
    fun document() = document

    /**
     * Navigates to URL and waits for it to load
     */
    fun navigateAndWait(url : String) : Document {
        return navigate(url)
                .waitForLoad()
    }

    override fun toString() : String {
        val info = mapOf(
            "debuggerUrl" to page.webSocketDebuggerUrl
        )

        return "Session{$info}"
    }

    /**
     * Returns identifier of the session.
     */
    open fun id() = "session{pageId=${page.id}}"

    /**
     * Closes connection to chrome debugger.
     */
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            logger.timedInfo(System.currentTimeMillis(), "Closing session $page")

            try {
                protocol.close()
                browser.closePage(page)
                logger.timedInfo(System.currentTimeMillis(), "Closed session $page")
            } catch (e : Exception) {
                logger.timedWarn(System.currentTimeMillis(), "Could not close session $page: ${e.message}")
            }
        }
    }

    /**
     * Checks if session is closed
     */
    fun isClosed() : Boolean = closed.get()

    /**
     * Returns logger used by session
     */
    fun logger() = logger
}