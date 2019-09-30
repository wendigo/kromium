package pl.wendigo.chrome.driver.rules

import org.junit.rules.ExternalResource
import org.testcontainers.containers.GenericContainer
import pl.wendigo.chrome.driver.SessionManager
import pl.wendigo.chrome.driver.session.Session

class TestSession extends ExternalResource {
    private SessionManager browser
    private Session session

    TestSession(GenericContainer container) {
        browser = new SessionManager.Companion().connect(container.getContainerIpAddress() + ":" + container.getFirstMappedPort())
    }

    @Override
    protected void before() throws Throwable {
        session = browser.newSession()
    }

    @Override
    protected void after() {
        session.close()
    }

    Session get() {
        return session
    }

    Session headless() {
        return browser.newIncognitoSession()
    }

    Session head() {
        return browser.newIncognitoSession()
    }
}
