package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.session.Session

class NodeInfoSpecification extends BaseSpecification {

    def "should return node type"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("textarea.area")

        expect:
            node.tagName() == "textarea"
    }

    def "should return node value"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("textarea.area")

        expect:
            node.value().trim() == "Hello world! How are you today?"

    }

    def "should return node box model"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/box"))
            def node = frame.query("#box")

        expect:
            with (node.boxModel()) {
                height == 50
                width == 50
                padding.toList() == [18.0, 10.0, 68.0, 10.0, 68.0, 60.0, 18.0, 60.0] as List<Double>
                margin.toList() == [8.0, 0.0, 1016.0, 0.0, 1016.0, 70.0, 8.0, 70.0] as List<Double>
            }
    }

    def "should return node inner text"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("body > p:nth-child(3)")

        expect:
            node.innerText() == "third\u00A0 node"
    }

    def "should return node text content"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("#second")

        expect:
            node.textContent().trim().stripIndent().split("\n")*.trim() == [
                    "1. first",
                    "2. second",
                    "3. third",
                    "4. forth",
                    "Hello world! How are you today?"
            ]
    }

    def "should return node inner/outer html"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("body > div:nth-child(4)")

        expect:
            node.innerHtml().trim() == "<p>first</p><p>second</p>"
            node.outerHtml().trim() == "<div><p>first</p><p>second</p></div>"
    }

    def "should return node attribute"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("body > div:nth-child(5)")

        expect:
            node.attribute("data-name").trim() == "name"
            node.attribute("counter").trim() == "1"
    }

    def "should return node attributes"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("body > div:nth-child(5)")
            def attributes = node.attributes()

        expect:
            attributes.size() == 2
            attributes.get("data-name") == "name"
            attributes.get("counter") == "1"
    }

    def "should check if element is enabled or disabled"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/enabled"))
            def nodes = frame.queryAll("input")

        expect:
            nodes.size() == 2

            with(nodes[0]) {
                !isEnabled().invoke()
                attribute("name") == "name"
            }

            with(nodes[1]) {
                isEnabled().invoke()
            }
    }

    def "should return false for isEnabled/isSelected on non-input field"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("div#second")

        expect:
            !node.isEnabled.invoke()
            !node.isSelected.invoke()
            !node.hasFocus.invoke()
    }

    def "should check if element is selected"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/enabled"))
            def nodes = frame.query("select[name=options]").queryAll("option")

        expect:
            nodes.size() == 3

            with(nodes[0]) {
                !isSelected.invoke()
            }

            with(nodes[1]) {
                isSelected.invoke()
            }

            with(nodes[2]) {
                !isSelected.invoke()
            }
    }

    def "should return root document from which node was created"() {
        given:
            def session = session.get()

        when:
            def document = session.navigateAndWait(server.staticAddress("demo"))

        then:
            document.document() == document
            document.query("body").query("p").document() == document
    }

    def "should return session in which node was created"() {
        given:
            def session = session.get()

        when:
            def document = session.navigateAndWait(server.staticAddress("demo"))

        then:
            document.document().session() == session
            document.query("body").query("p").session() == session
    }


    def sessionContext = GroovyMock(SessionContext)

    def "should throw exception on artifically created node"() {
        given:
            def node = new Node(null, "", 0, sessionContext)

        when:
            node.document()

        then:
            def exception = thrown(IllegalStateException)
            exception.message == "Could not find root document while traversing node hierarchy"
    }
}