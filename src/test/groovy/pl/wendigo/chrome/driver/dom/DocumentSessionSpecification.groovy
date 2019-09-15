package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class DocumentSessionSpecification extends BaseSpecification {

    def "should return valid navigation history"() {
        given:
            def session = session.get()
            def document = session
                .navigateAndWait(server.staticAddress("demo"))
                .reload(true)
                .reload(true)

        when:
            def history = document.history()

        then:
            history.size() == 2
            history*.url == ["about:blank", server.staticAddress("demo")]
    }

    def "document should return proper session when created"() {
        given:
            def session = session.get()

        when:
            def document = session.navigate(server.staticAddress("demo"))

        then:
            document.session() == session
    }

    def "should navigate back and forth"() {
        given:
            def session = session.get()

        when:
            def document = session
                    .navigateAndWait(server.staticAddress("demo"))
                    .navigateAndWait(server.staticAddress("hello"))

        then:
            document.history().size() == 3
            document.navigateBack().waitForLoad().location() == server.staticAddress("demo")
            document.navigateForward().waitForLoad().location() == server.staticAddress("hello")
    }

    def "should throw exception if navigating back too far"() {
        given:
            def session = session.get()
            def document = session.navigateAndWait(server.staticAddress("demo"))

        when:
            document.navigateBack().navigateBack()

        then:
            def exception = thrown(IllegalStateException)
            exception.message == "Could not navigate back in history, current index: 0, history length: 2"

    }

    def "should throw exception if navigating forward too far"() {
        given:
            def session = session.get()
            def document = session.navigateAndWait(server.staticAddress("demo"))

        when:
            document.navigateForward()

        then:
            def exception = thrown(IllegalStateException)
            exception.message == "Could not navigate forward in history, current index: 1, history length: 2"
    }

    def "should close session only once"() {
        given:
            def session = session.get()
            def document = session.navigateAndWait(server.staticAddress("demo"))

        expect:
            !document.session().isClosed()

        when:
            document.close()
            document.close()

        then:
            session.isClosed()
    }
}
