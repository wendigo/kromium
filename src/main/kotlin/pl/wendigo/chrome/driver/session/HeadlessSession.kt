package pl.wendigo.chrome.driver.session

import org.slf4j.LoggerFactory
import pl.wendigo.chrome.Browser
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
                type = "",
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

    override fun close() {
        try {
            super.close()
            protocol.close()
        } catch (e : Throwable) {
            logger.warn("Caught exception while closing session {}", e.message)
        }
    }

    /**
     * Returns identifier of the headless session.
     */
    override fun id(): String {
        return "headless{pageId=${protocol.sessionDescriptor.sessionId}, session=${protocol.sessionDescriptor}}"
    }
}