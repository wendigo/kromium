package pl.wendigo.chrome.driver.session

import org.slf4j.LoggerFactory
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.driver.timedInfo
import pl.wendigo.chrome.driver.timedWarn
import pl.wendigo.chrome.headless.HeadlessDevToolsProtocol
import pl.wendigo.chrome.protocol.inspector.InspectablePage

class HeadlessSession constructor(
    private val protocol: HeadlessDevToolsProtocol,
    private val browser : Browser
) : Session(
        InspectablePage(
                description = "",
                url = protocol.sessionDescriptor.url,
                id = protocol.sessionDescriptor.sessionId,
                title = "",
                type = "page",
                webSocketDebuggerUrl = "",
                devtoolsFrontendUrl = ""
        ),
        protocol,
        browser,
        LoggerFactory.getLogger("headless-${protocol.sessionDescriptor.sessionId}")
) {
    override fun toString() : String {
        val info = mapOf(
                "debuggerUrl" to browser.version().webSocketDebugUrl,
                "url" to protocol.sessionDescriptor.url,
                "width" to protocol.sessionDescriptor.width,
                "height" to protocol.sessionDescriptor.height,
                "targetId" to protocol.sessionDescriptor.targetId,
                "browserContextId" to protocol.sessionDescriptor.browserContextId,
                "sessionId" to protocol.sessionDescriptor.sessionId
        )

        return "headlessSession{$info}"
    }

    /**
     * Closes connection to chrome debugger.
     */
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            logger.timedInfo(System.currentTimeMillis(), "Closing session ${protocol.sessionDescriptor}")

            try {
                protocol.close()
                logger.timedInfo(System.currentTimeMillis(), "Closed session ${protocol.sessionDescriptor}")
            } catch (e : Exception) {
                logger.timedWarn(System.currentTimeMillis(), "Could not close session ${protocol.sessionDescriptor}: ${e.message}")
            }
        }
    }

    /**
     * Returns identifier of the headless session.
     */
    override fun id(): String {
        return "headless{pageId=${protocol.sessionDescriptor.sessionId}, session=${protocol.sessionDescriptor}}"
    }
}