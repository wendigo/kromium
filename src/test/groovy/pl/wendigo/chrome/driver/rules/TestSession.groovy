package pl.wendigo.chrome.driver.rules

import org.junit.rules.ExternalResource
import pl.wendigo.chrome.driver.SessionManager
import pl.wendigo.chrome.driver.session.Session

class TestSession extends ExternalResource {
    private static SessionManager browser
    private Session session

    static  {
        def address = System.getenv("HEADLESS_ADDRESS")

        if (address == null) {
            address = "localhost:9222"
        }

        browser = new SessionManager.Companion().connect(address)
    }

    @Override
    protected void before() throws Throwable {
        session = browser.newHeadlessSession(1024, 768, 128)
    }

    @Override
    protected void after() {
        session.close()
    }

    Session get() {
        return session
    }

    Session headless() {
        return browser.newHeadlessSession(1024, 769, 128)
    }

    Session head() {
        return browser.newSession(128)
    }
}
