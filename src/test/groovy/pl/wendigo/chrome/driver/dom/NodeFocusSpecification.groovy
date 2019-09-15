package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class NodeFocusSpecification extends BaseSpecification {
    def "should check if active element is focused"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/focus/index"))
            def inputs = frame.queryAll("input")

        expect:
            !inputs[0].hasFocus.invoke()
            !inputs[1].hasFocus.invoke()
            inputs[2].hasFocus.invoke()
    }

    def "should focus on inactive element"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/focus/index"))
            def inputs = frame.queryAll("input")

        expect:
            inputs[0].focus().hasFocus.invoke()
            inputs[1].focus().hasFocus.invoke()
            inputs[2].focus().hasFocus.invoke()
    }
}
