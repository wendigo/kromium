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
