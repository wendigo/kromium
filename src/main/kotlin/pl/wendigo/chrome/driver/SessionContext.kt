package pl.wendigo.chrome.driver

import org.slf4j.Logger
import pl.wendigo.chrome.DevToolsProtocol
import pl.wendigo.chrome.api.network.EnableRequest
import pl.wendigo.chrome.api.page.SetLifecycleEventsEnabledRequest
import pl.wendigo.chrome.driver.session.Session

class SessionContext (
    val logger: Logger,
    val protocol: DevToolsProtocol,
    val session: Session
) {
    internal val enableDOM by lazy {
        protocol.DOM.enable().cache()
    }

    internal val enableNetwork by lazy {
        protocol.Network.enable(EnableRequest()).cache()
    }

    internal val enablePage by lazy {
        protocol.Page.enable().flatMap {
            protocol.Page.setLifecycleEventsEnabled(SetLifecycleEventsEnabledRequest(true))
        }.cache()
    }

    internal val enableDomains by lazy {
        enableDOM.flatMap {
            enableNetwork
        }.flatMap {
            enablePage
        }
    }
}