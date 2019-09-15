package pl.wendigo.chrome.driver.rules

import groovy.servlet.GroovyServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.junit.rules.ExternalResource

import java.util.concurrent.atomic.AtomicReference

class StaticServer {
    private static AtomicReference<StaticServer> INSTANCE = new AtomicReference<>(null)

    private Server server
    private String localAddress

    StaticServer() {
        server = new Server(0)

        def handler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        handler.contextPath = '/'
        handler.resourceBase = './src/test/resources/'
        handler.addServlet(GroovyServlet, '/scripts/*')

        def filesHolder = handler.addServlet(DefaultServlet, '/')
        filesHolder.setInitParameter('resourceBase', './src/test/resources')

        server.handler = handler
        server.start()

        //def address = System.getenv("LOOPBACK_ADDRESS")
        def address = "192.168.1.63"

        if (address == null) {
            localAddress = "http://${InetAddress.getLocalHost().getHostAddress()}:${getPort()}/"
        } else {
            localAddress = "http://$address:${getPort()}/"
        }
    }

    def getPort() {
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort()
    }

    def address(String url) {
        return localAddress + url
    }

    def staticAddress(String file) {
        return address("static/" + file + ".html")
    }

    def scriptAddress(String script) {
        return address("scripts/" + script + ".groovy")
    }

    static StaticServer instance() {
        if (INSTANCE.get() == null) {
            INSTANCE.compareAndSet(null, new StaticServer())
        }

        return INSTANCE.get()
    }
}