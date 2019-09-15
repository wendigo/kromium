package pl.wendigo.chrome.driver.intercept

import pl.wendigo.chrome.driver.BaseSpecification

class FirstMatchingOnlySpecification extends BaseSpecification {

    def "should accept only first matching element"() {
        given:
            def filter = new FirstMatchingOnly(new BooleanMatching(true))

        expect:
            filter.accept("first")
            !filter.accept("second")
            filter.description() == "FirstMatchingOnly(BooleanMatching(true))"
    }

    def "should not accept if inner filter returns false"() {
        given:
            def filter = new FirstMatchingOnly(new BooleanMatching(false))

        expect:
            !filter.accept("first")
            !filter.accept("second")
    }

    class BooleanMatching implements InterceptFilter<String> {

        private boolean value

        BooleanMatching(Boolean value) {
            this.value = value
        }

        @Override
        String description() {
            return "BooleanMatching($value)"
        }

        @Override
        boolean accept(String s) {
            return value
        }
    }
}
