package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class NodeBoxModelSpecification extends BaseSpecification {

    def "should compute position for onscreen node"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/boxmodel/basic"))
            def node = frame.query("div#onscreen")
            def position = node.position()

        expect:
            position.left == 96
            position.top == 92
    }

    def "should compute position for offscreen node"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/boxmodel/basic"))
            def node = frame.query("div#offscreen")
            def position = node.position()

        expect:
            position.left == 2075
            position.top == 3075
    }

    def "should scroll to offscreen node"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/boxmodel/basic"))
            def node = frame.query("div#offscreen")
            def position = node.scrollTo(false).position()

        expect:
            position.left == 949
            position.top == 693
    }

    def "should return true for visible node"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/boxmodel/basic"))
            def node = frame.query("div#offscreen")
            def node2 = frame.query("div#onscreen")
            def node3 = frame.query("div#hidden")

        expect:
            node.isVisible.invoke()
            node2.isVisible.invoke()
            !node3.isVisible.invoke()

    }
}
