package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class DocumentInfoSpecification extends BaseSpecification {
    def "should return document title"() {
        given:
            def frame = session.get()
            def document = frame.navigateAndWait(server.staticAddress("hello"))

        when:
            def title = document.title()

        then:
            title == "Hello World Page Title"
    }

    def "should return document location"() {
        given:
            def session = session.get()
            def document = session.navigateAndWait(server.staticAddress("hello"))

        when:
            def url = document.location()

        then:
            url == server.staticAddress("hello")
    }
}
