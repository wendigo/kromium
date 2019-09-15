package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class NodeTypeSpecification extends BaseSpecification {
    def "should type chars into field"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/type/index"))

        when:
            frame.query("input")
                    .focus()
                    .type(5, "Mat".toCharArray(), "eusz\t".toCharArray())
                    .type(5, "contact@serafin.tech\t", "Gajewski\t")

            def inputs = frame.queryAll("input")

        then:
            inputs*.attribute("name") == ["name", "surname", "email"]
            inputs*.value() == ["Mateusz", "Gajewski", "contact@serafin.tech"]
    }

    def "should type all alphabet chars into field"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/type/field"))
            def input = ('a'..'z').join("") + ('A'..'Z').join("") + "zażółć gęślą jaźń ZAŻÓŁĆ GĘŚLĄ JAŹŃ ❤"

        when:
            frame
                .query("textarea")
                .focus()
                .type(input)

        then:
            frame.query("textarea").value() == input

    }

    def "should type all alphanumeric chars into field"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/type/field"))
            def input = ('0'..'9').join("") +  "`~!@#\$%^&*()-=_+[]{}:;\"\'\\|/?,<>.\n "

        when:
            frame
                .query("textarea")
                .focus()
                .type(input)

        then:
            frame.query("textarea").value() == input
    }
}