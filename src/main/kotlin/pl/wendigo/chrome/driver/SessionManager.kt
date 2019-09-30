package pl.wendigo.chrome.driver

import org.slf4j.LoggerFactory
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.driver.session.Session

class SessionManager(private val browser: Browser) {
    fun newSession() : Session {
        logger.timedInfo(System.currentTimeMillis(), "Opening new session...")

        val page = browser.target(incognito = false)

        return Session(page, browser).also {
            logger.timedInfo(System.currentTimeMillis(), "Opened new session $this")
        }
    }

    fun newIncognitoSession() : Session {
        logger.timedInfo(System.currentTimeMillis(), "Opening new incognito session...")

        val page = browser.target(incognito = true)

        return Session(page, browser).also {
            logger.timedInfo(System.currentTimeMillis(), "Opened new incognito session $this")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger("browser")

        fun connect(remoteChrome : String) : SessionManager {
            return SessionManager(Browser.builder().withAddress(remoteChrome).build())
        }

        fun connect(browser: Browser) : SessionManager {
            return SessionManager(browser)
        }
    }
}