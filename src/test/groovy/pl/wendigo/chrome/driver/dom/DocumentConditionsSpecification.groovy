package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class DocumentConditionsSpecification extends BaseSpecification {

    def "should wait for window to load"() {
        given:
            def frame = session.get().navigate(server.scriptAddress("dom/runtime/index"))

        expect:
           !frame.check(frame.documentLoaded())

        when:
            frame.await(10000,50, frame.documentLoaded())

        then:
            frame.check(frame.documentLoaded())
            frame.check(frame.documentLoaded())
    }

    def "should wait for window to stop network activity"() {
        given:
            def frame = session.get().navigate(server.staticAddress("dom/runtime/network"))

        expect:
            !frame.check(frame.networkIdle())

        when:
            frame.await(10000,50, frame.networkIdle())

        then:
            frame.check(frame.networkIdle())
            frame.check(frame.networkIdle())
    }

    def "should wait for node to appear"() {
        given:
            def frame = session.get().navigate(server.staticAddress("dom/runtime/appearing"))

        when:
            frame.await(
                1000,
                frame.selectorExists("div#appearing"),
                frame.selectorExists("div#appearing2")
            )

        then:
            frame.check(frame.selectorExists("div#appearing"))
            frame.check(frame.selectorExists("div#appearing2"))
    }

    def "should wait for node to gain focus"() {
        given:
            def frame = session.get().navigate(server.staticAddress("dom/runtime/focusing"))

        expect:
            !frame.check(frame.selectorHasFocus("input#focus"))

        when:
            frame.await(
                1000,
                frame.selectorHasFocus("input#focus")
            )

        then:
            frame.check(frame.selectorHasFocus("input#focus"))
    }

    def "should wait for node to become visible"() {
        given:
            def frame = session.get().navigate(server.staticAddress("dom/runtime/appearing2"))

        when:
            frame.await(
                500,
                frame.selectorExists("div#loading > div#appearing")
            )

            frame.await(
                    600,
                    frame.selectorIsVisible("div#loading > div#appearing")
            )

        then:
            frame.check(frame.and(
                    frame.selectorIsVisible("div#appearing"),
                    frame.selectorExists("div#appearing")
            ))
    }
}