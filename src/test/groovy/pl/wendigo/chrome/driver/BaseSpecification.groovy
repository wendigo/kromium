package pl.wendigo.chrome.driver

import org.junit.Rule
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import pl.wendigo.chrome.driver.rules.StaticServer
import pl.wendigo.chrome.driver.rules.TestSession
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class BaseSpecification extends Specification {
    StaticServer server = StaticServer.instance()

    @Shared
    public GenericContainer container = new GenericContainer("zenika/alpine-chrome:76")
            .withExposedPorts(9222)
            .withCommand(
                    "chromium-browser",
                    "--headless",
                    "--disable-gpu",
                    "--disable-software-rasterizer",
                    "--disable-dev-shm-usage",
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--remote-debugging-port=9222",
                    "--remote-debugging-address=0.0.0.0",
                    "about:blank"
            )
            .withPrivilegedMode(true)

    @Rule
    TestSession session = new TestSession(container)
}