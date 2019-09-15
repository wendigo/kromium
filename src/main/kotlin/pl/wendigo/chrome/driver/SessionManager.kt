package pl.wendigo.chrome.driver

import org.slf4j.LoggerFactory
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.driver.session.HeadlessSession
import pl.wendigo.chrome.driver.session.Session

class SessionManager(private val browser: Browser) {
    fun newSession(bufferSize : Int = 128) : Session {
        logger.timedInfo(System.currentTimeMillis(), "Opening new session {bufferSize=$bufferSize}")

        val page = browser.openNewPage(BLANK_PAGE_LOCATION)

        return Session(page, page.session(bufferSize), browser).also {
            logger.timedInfo(System.currentTimeMillis(), "Opened new session $this")
        }
    }

    fun newHeadlessSession(width : Int = 1024, height : Int = 768, bufferSize : Int = 128) : Session {
        logger.timedInfo(System.currentTimeMillis(), "Opening new headless session {width=$width, height=$height, bufferSize=$bufferSize}")

        val protocol = browser.headlessSession(BLANK_PAGE_LOCATION, bufferSize, width, height).also {
            logger.timedInfo(System.currentTimeMillis(), "Opened new page for headless session $this")
        }

        return HeadlessSession(protocol, browser).also {
            logger.timedInfo(System.currentTimeMillis(), "Opened new headless session $this")
        }
    }

    companion object {
        const val BLANK_PAGE_LOCATION = "about:blank"
        private val logger = LoggerFactory.getLogger("browser")

        fun connect(remoteChrome : String) : SessionManager {
            return SessionManager(Browser.connect(remoteChrome))
        }
    }
}