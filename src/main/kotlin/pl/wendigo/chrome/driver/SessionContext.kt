package pl.wendigo.chrome.driver

import org.slf4j.Logger
import pl.wendigo.chrome.api.network.EnableRequest
import pl.wendigo.chrome.api.page.SetLifecycleEventsEnabledRequest
import pl.wendigo.chrome.driver.session.Session
import pl.wendigo.chrome.targets.Target

class SessionContext (
    val logger: Logger,
    val target: Target,
    val session: Session
) {
    internal val enableDOM by lazy {
        target.DOM.enable().cache()
    }

    internal val enableNetwork by lazy {
        target.Network.enable(EnableRequest()).cache()
    }

    internal val enablePage by lazy {
        target.Page.enable().flatMap {
            target.Page.setLifecycleEventsEnabled(SetLifecycleEventsEnabledRequest(true))
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