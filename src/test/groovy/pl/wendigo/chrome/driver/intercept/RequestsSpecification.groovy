package pl.wendigo.chrome.driver.intercept

import kotlin.jvm.functions.Function2
import pl.wendigo.chrome.driver.BaseSpecification
import pl.wendigo.chrome.driver.dom.Node
import pl.wendigo.chrome.driver.intercept.network.Requests
import pl.wendigo.chrome.driver.intercept.network.UrlContains

class RequestsSpecification extends BaseSpecification {
    def "should describe filters"() {
        given:
            def filter = new Not(new And(
                    new Or(new UrlContains("hello"), new UrlContains("world")),
                    new UrlContains("serafin")
            ))

        expect:
            filter.description() == "~((UrlContains(hello) || UrlContains(world)) && UrlContains(serafin))"
    }

    def "should not intercept network call if filter returns false"() {
        given:
            def frame = session.get().document()

        when:

            def closure = { node, interceptor ->
                node.navigate(server.staticAddress("intercept/requests"))

                node.await(2000, frame.selectorCondition("#response", {
                    it.innerHtml() == "Hello world!" && it.isVisible()
                }))

            } as Function2<Node, Requests, Boolean>

            def captured = frame.intercept(new Requests(), new UrlContains("notExisting.json"), closure)


        then:
            captured.size() == 0
    }

    def "should intercept network call"() {
        given:
            def frame = session.get().document()

        when:

            def closure = { node, interceptor ->
                node.navigateAndWait(server.staticAddress("intercept/requests"))

                node.await(2000, frame.selectorCondition("#response", {
                    it.innerHtml() == "Hello world!" && it.isVisible()
                }))
            } as Function2<Node, Requests, Boolean>

            def captured = frame.intercept(new Requests(), new UrlContains("query.json"), closure)

        then:
            captured.size() == 1

            captured[0].url =~ "query.json\$"
            captured[0].body == "Hello world!"
    }

    def "should await for intercepted network call"() {
        given:
            def frame = session.get().document()

        when:
            def closure = { node, Requests interceptor ->
                node.navigateAndWait(server.staticAddress("intercept/requests"))
                node.await(2000, interceptor.capturedExactly(1))
            } as Function2<Node, Requests, Boolean>

         def captured = frame.intercept(new Requests(), new UrlContains("query.json"), closure)

        then:
            captured.size() == 1
            captured[0].url =~ "query.json\$"
            captured[0].body == "Hello world!"
    }

    def "should await for intercepting multiple network calls"() {
        given:
            def frame = session.get().document()

        when:
            def closure = { node, Requests interceptor ->
                node.navigateAndWait(server.staticAddress("intercept/requests_multiple"))
                node.await(1000, interceptor.capturedAtLeast(5))
            } as Function2<Node, Requests, Boolean>

            def captured = frame.intercept(new Requests(), new UrlContains("query.json"), closure)

        then:
            captured.size() >= 5
    }
}
