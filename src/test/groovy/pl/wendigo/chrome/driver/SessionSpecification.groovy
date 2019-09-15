package pl.wendigo.chrome.driver

class SessionSpecification extends BaseSpecification {

    def "should always return same document"() {
        given:
            def session = session.get()

        when:
            def document1 = session.navigate(server.staticAddress("hello"))
            def document2 = session.navigate(server.staticAddress("hello"))

        then:
            document1 == document2
    }

    def "should override user agent"() {
        given:
            def userAgent = "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko"
            def document = session.get()
                    .userAgent(userAgent)
                    .navigateAndWait(server.staticAddress("session/useragent"))

        when:
            document.await(document.selectorCondition("#userAgent", {
                it.innerHtml() != ""
            }))

        then:
            document.query("#userAgent").innerHtml() == userAgent
    }

    def "should return frame while navigating to page"() {
        given:
            def document = session.get()
                    .navigate(server.staticAddress("hello"))
                    .waitForLoad()
        expect:
            document.nodeId() > 0
    }

    def "should disallow loading resources from block url list"() {
        given:
            def document = session.get()
                .blockUrls("*query.json")
                .navigate(server.staticAddress("session/blocked"))
                .waitForLoad()
        when:
            document.await(document.selectorCondition("#response", {
                return it.isVisible().invoke()
            }))

        then:
            document.query("#response").innerHtml() == "TypeError: Failed to fetch"
    }

    def "should return google certificate list when loading by https"() {
        given:
            def session = session.get()

        when:
            session.navigate("https://www.google.pl")
                .waitForLoad()

        then:
            def certificates = session.certificates("https://www.google.pl")

            certificates.size() > 0

            certificates*.issuerDN*.name == [
                    "CN=GTS CA 1O1, O=Google Trust Services, C=US",
                    "CN=GlobalSign, O=GlobalSign, OU=GlobalSign Root CA - R2",
                    "CN=GlobalSign, O=GlobalSign, OU=GlobalSign Root CA - R2"
            ]

    }

    def "should return frame while navigating and waiting for page"() {
        given:
            def frame = session.get()
                .navigateAndWait(server.staticAddress("hello"))
        expect:
            frame.nodeId() > 0
    }

    def "should return same frame nodeId on multiple requests"() {
        given:
            def frame = session.get()
                .navigateAndWait(server.staticAddress("hello"))
        expect:
            frame.nodeId() == frame.nodeId()
    }

    def "should load new document id when document is invalidated"() {
        given:
            def frame = session.get()
            def document = frame.navigateAndWait(server.staticAddress("hello"))
            def nodeId = document.nodeId()

        when:
            def document2 = document.navigateAndWait(server.staticAddress("demo"))

        then:
            document2.nodeId() > nodeId
    }

    def "should load new document id when document is navigated"() {
        given:
            def frame = session.get()
            def document = frame.navigateAndWait(server.staticAddress("hello"))
            def nodeId = document.nodeId()

        when:
            def document2 = document.navigateAndWait(server.staticAddress("hello"))

        then:
            document2.nodeId() > nodeId
    }

    def "should load new document id when document is reloaded"() {
        given:
            def frame = session.get()
            def document = frame.navigateAndWait(server.staticAddress("hello"))
            def nodeId = document.nodeId()

        when:
            def document2 = document.reload(true).waitForLoad()

        then:
            document2.nodeId() > nodeId
    }

    def "should stay loaded when changing url hash"() {
        given:
            def frame = session.get()
            def document = frame
                    .navigateAndWait(server.staticAddress("hello"))

        when:
            document.await(document.documentLoaded())
            document.navigateAndWait(server.staticAddress("hello") + "#hashpart")

        then:
            document.documentLoaded().invoke()
    }

    def "should separate three sessions from each other"() {
        setup:
            def session1 = session.headless()
            def session2 = session.headless()
            def session3 = session.headless()

            def doc1 = session1.navigate(server.staticAddress('session/counter'))
            def doc2 = session2.navigate(server.staticAddress('session/counter'))
            def doc3 = session3.navigate(server.staticAddress('session/counter'))

        when:
            doc1.await(2000, doc1.selectorCondition("#counter",{
                it.innerHtml() == "99"
            }))

            doc2.await(2000, doc2.selectorCondition("#counter",{
                it.innerHtml() == "99"
            }))

            doc3.await(2000, doc3.selectorCondition("#counter",{
                it.innerHtml() == "99"
            }))

        then:
            doc1.query("#storage").innerHtml().toInteger() <= 100
            doc2.query("#storage").innerHtml().toInteger() <= 100
            doc3.query("#storage").innerHtml().toInteger() <= 100

        cleanup:
            session1.close()
            session2.close()
            session3.close()
    }
}