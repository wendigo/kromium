package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.driver.BaseSpecification

class NodeClickSpecification extends BaseSpecification {

    def "should click element multiple times using native events"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/click/click"))
            def node = frame.query("div#click")
            def numberOfClicks = 100

        when:
            node.logger.info("Started clicking")
            1.upto(numberOfClicks, {
                node.click(2, true)
            })
            node.logger.info("Ended clicking")

        then:
            def clickCount = node.attribute("click-count").toInteger()
            node.logger.info("Registered click count is ${clickCount}")
            clickCount > 75
    }

    def "should click element multiple times using synthetic events"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/click/click"))
            def node = frame.query("div#click")
            def numberOfClicks = 100

        when:
            node.logger.info("Started clicking")
            1.upto(numberOfClicks, {
                node.click(2, false)
            })
            node.logger.info("Ended clicking")

        then:
            def clickCount = node.attribute("click-count").toInteger()
            node.logger.info("Registered click count is ${clickCount}")
            clickCount == 100
    }
}
