package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification
import pl.wendigo.chrome.driver.session.SessionClosedException

import java.util.concurrent.TimeoutException

class NodeConditionsSpecification extends BaseSpecification {
    def "should check all conditions"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/enabled"))
            def node = frame.query("select[name=options]")

        expect:
            node.check(node.isEnabled, node.isVisible)
            frame.check(node.isEnabled, node.isVisible)
            !node.check(node.isEnabled, node.isSelected, node.isVisible)
    }

    def "should check condition with lambda"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("div#second")

        expect:
            frame.check(node.condition {
                !it.hasFocus.invoke() && it.tagName() == "div" && it.innerHtml().contains("Hello world")
            })

            node.check(node.condition {
                !it.hasFocus.invoke() && it.tagName() == "div" && it.innerHtml().contains("Hello world")
            })
    }

    def "should apply logic conditions properly"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = frame.query("div#second")

        expect:
            node.not(node.hasFocus).invoke()
            node.not(node.isSelected).invoke()
            node.not(node.and(node.isSelected, node.isEnabled)).invoke()
            node.or(node.isVisible, node.isSelected).invoke()
            !node.or(node.isSelected, node.hasFocus).invoke()
    }

    def "should describe conditions"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/info/basic"))
            def node = document.query("div#second")

        expect:
            def rootId = document.nodeId()
            def nodeId = rootId + 6

            node.isSelected.description() == "isSelected(Node($nodeId)[html($rootId) > div#second($nodeId)])"
            node.hasFocus.description() == "hasFocus(Node($nodeId)[html($rootId) > div#second($nodeId)])"
            node.isVisible.description() == "isVisible(Node($nodeId)[html($rootId) > div#second($nodeId)])"
            node.isSelected.description() == "isSelected(Node($nodeId)[html($rootId) > div#second($nodeId)])"
            node.isEnabled.description() == "isEnabled(Node($nodeId)[html($rootId) > div#second($nodeId)])"
            node.not(node.isEnabled).description() == "~isEnabled(Node($nodeId)[html($rootId) > div#second($nodeId)])"

            node.condition {
                it.isEnabled().invoke()
            }.description() ==~ "^condition(.*)\$"

            node.and(
                    node.isSelected,
                    node.isVisible,
                    node.not(node.isEnabled)
            ).description() == "(isSelected(Node($nodeId)[html($rootId) > div#second($nodeId)]) && isVisible(Node($nodeId)[html($rootId) > div#second($nodeId)]) && ~isEnabled(Node($nodeId)[html($rootId) > div#second($nodeId)]))"

            node.or(
                    node.not(node.isSelected),
                    node.isVisible
            ).description() == "(~isSelected(Node($nodeId)[html($rootId) > div#second($nodeId)]) || isVisible(Node($nodeId)[html($rootId) > div#second($nodeId)]))"

            document.selectorCondition("div#second", {
                it.isVisible()
            }).description() ==~ "selectorCondition\\(div#second, (.*)\\)"
    }

    def "should wait for all conditions"() {

        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))
            def node = frame.query("input#waitFor")

        expect:
            def awaited = node.await(1500, 100, node.isVisible, node.hasFocus)

            awaited.isVisible.invoke()
            awaited.hasFocus.invoke()
    }

    def "should wait for conditions and return status"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))
            def node = frame.query("input#waitFor")

        expect:
            node.tryAwait(1500, 100, node.isVisible, node.hasFocus)
            node.isVisible().invoke()
            node.hasFocus.invoke()
    }

    def "should wait for all conditions using selectorCondition"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))

        expect:
            frame.await(1500, frame.selectorCondition("input#waitFor", {
                it.isVisible.invoke() && it.hasFocus.invoke()
            }))

            def input = frame.query("input#waitFor")
            input.check(input.hasFocus, input.isVisible)
    }

    def "should wait for all conditions using selectorCondition and return node"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))

        expect:
            frame.awaitSelector(1500, "input#waitFor",{
                it.isVisible.invoke() && it.hasFocus.invoke()
            }).isVisible().invoke()
    }

    def "should wait for all conditions using selectorCondition in await"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))

        expect:
            frame.await(1500,"input#waitFor", {
                it.isVisible.invoke() && it.hasFocus.invoke()
            })

        def input = frame.query("input#waitFor")
            input.check(input.hasFocus, input.isVisible)
    }

    def "should throw timeout exception if conditions were not met"() {

        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))
            def node = document.query("input#waitFor")

        when:
            node.await(200, 100, node.isVisible, node.hasFocus)

        then:
            def rootId = document.nodeId()
            def nodeId = rootId + 5

            def exception = thrown(TimeoutException)
            exception.message == "Conditions were not satisfied [isVisible(Node($nodeId)[html($rootId) > input#waitFor($nodeId)]), hasFocus(Node($nodeId)[html($rootId) > input#waitFor($nodeId)])]"
    }

    def "should throw session closed exception when session was closed"() {

        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))
            def node = document.query("input#waitFor")

        when:
            new Thread({
                Thread.sleep(100)
                document.session().close()
            }).start()

            node.await(20000, node.selectorCondition("#notexisting", {
                it.hasFocus.invoke()
            }))

        then:
            def exception = thrown(SessionClosedException)
            exception.message == "Session is already closed, cannot invoke operation"
    }

    def "should return false when conditions were not met"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/info/wait"))
            def node = document.query("input#waitFor")

        when:
            def met = node.tryAwait(200, 100, node.isVisible, node.hasFocus)

        then:
            !met
    }
}
