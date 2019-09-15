package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification
import pl.wendigo.chrome.driver.EvaluationFailed

class DocumentRuntimeSpecification extends BaseSpecification {

    def "should execute javascript on document"() {
        given:
            def frame = session.get()
                    .navigateAndWait(server.staticAddress("dom/runtime/index"))

        when:
            def object = frame.evaluate("(function() { return document.title; })();")

        then:
            object.value.toString() == "Hello this is title"
    }

    def "should throw exception on invalid javascript"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/runtime/index"))

        when:
            frame.evaluate("should not compile;")

        then:
            def exception = thrown(EvaluationFailed)
            exception.message == "SyntaxError: Unexpected identifier"
    }
}