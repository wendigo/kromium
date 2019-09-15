package pl.wendigo.chrome.driver

import org.junit.Rule
import pl.wendigo.chrome.driver.rules.StaticServer
import pl.wendigo.chrome.driver.rules.TestSession
import spock.lang.Specification

class BaseSpecification extends Specification {
    StaticServer server = StaticServer.instance()

    @Rule
    TestSession session = new TestSession()
}