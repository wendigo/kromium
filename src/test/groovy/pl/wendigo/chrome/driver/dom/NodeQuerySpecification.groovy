package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification
import spock.lang.Ignore

class NodeQuerySpecification extends BaseSpecification {
    def "should find a node in a given document"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("demo"))

        when:
            def node = frame.query("p")
            def node2 = frame.query("p")

        then:
            node.nodeId() == node2.nodeId()
    }

    def "should throw exception if node is not found"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("demo"))

        when:
            frame.query("div.nonexistent")

        then:
            def exception = thrown(NoSuchElementException)
            exception.message ==~ "Node not found for selector div.nonexistent and parent node [0-9]+"

    }

    def "should throw exception if node is not found with xpath expression"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("demo"))

        when:
            frame.xpath("//div[@class=\"nonexistent\"]")

        then:
            def exception = thrown(NoSuchElementException)
            exception.message ==~ """Node not found for selector //div\\[@class="nonexistent"\\] and parent node [0-9]+"""
    }

    def "should find multiple nodes in a given document"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/multiple"))

        when:
            def nodes = frame.queryAll("p")

        then:
            nodes.size() == 3

            nodes[0].nodeId() == frame.nodeId() + 4
            nodes[1].nodeId() == frame.nodeId() + 6
            nodes[2].nodeId() == frame.nodeId() + 8
    }

    def "should find ascending nodes"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))

        when:
            def nodes = frame
                .query("div#second")
                .queryAll("div")

        then:
            nodes.size() == 4
    }

    def "should return node path"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))

        when:
            def nodes = frame
                .query("div#second")
                .queryAll("div")

        then:
            nodes*.path() == [
                "html(${frame.nodeId()}) > div#second(${frame.nodeId() + 6}) > div(${frame.nodeId() + 9})",
                "html(${frame.nodeId()}) > div#second(${frame.nodeId() + 6}) > div(${frame.nodeId() + 11})",
                "html(${frame.nodeId()}) > div#second(${frame.nodeId() + 6}) > div(${frame.nodeId() + 13})",
                "html(${frame.nodeId()}) > div#second(${frame.nodeId() + 6}) > div(${frame.nodeId() + 15})"
            ]
    }

    def "should format node to string"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))

        when:
            def node = document
                .query("div#second")
                .query("div")

        then:
            node.toString() == "Node(${document.nodeId() + 9})[html(${document.nodeId()}) > div#second(${document.nodeId() + 6}) > div(${document.nodeId() + 9})]"
    }

    def "should return node selector"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))

        when:
            def node = frame
                .query("div#second")

        then:
            node.selector() == "div#second"
    }

    def "should return children"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))

        when:
            def nodes = frame
                .query("div#second").children(1)

        then:
            nodes*.selector() == ["div#second:nth-child(0)", "div#second:nth-child(1)", "div#second:nth-child(2)", "div#second:nth-child(3)"]
            nodes*.innerHtml() == ["1. first", "2. second", "3. third", "4. forth"]
    }

    def "should find node by xpath query"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))
                frame.await(frame.documentLoaded())

        when:
            def node = frame
                .xpath("//div[@id='second']")
        then:
            node.selector() == "//div[@id='second']"
    }

    def "should find nodes by xpath query"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/query/ascending"))
            frame.await(frame.documentLoaded())

        when:
            def nodes = frame
                .xpathAll("//div")
        then:
            nodes.size() == 5
    }
}
