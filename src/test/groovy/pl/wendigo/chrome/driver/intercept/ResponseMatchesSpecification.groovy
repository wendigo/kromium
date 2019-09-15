package pl.wendigo.chrome.driver.intercept

import pl.wendigo.chrome.api.network.Response
import pl.wendigo.chrome.driver.BaseSpecification
import pl.wendigo.chrome.driver.intercept.network.ResponseMatches

class ResponseMatchesSpecification extends BaseSpecification {

    def mock = GroovyMock(Response)

    def "should accept only if lambda returns true"() {

        given:
            def filter = new ResponseMatches({
                it.url == "/matching/url"
            })

            mock.url >> "/matching/url"

        expect:
            filter.accept(mock)
    }

    def "should not accept if lambda returns false"() {

        given:
            def filter = new ResponseMatches({
                it.url == "/matching/url"
            })

            mock.url >> "/not/matching/url"

        expect:
            !filter.accept(mock)
    }
}
