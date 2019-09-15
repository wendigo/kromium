package pl.wendigo.chrome.driver.intercept

import pl.wendigo.chrome.driver.BaseSpecification

class ConditionsSpecification extends BaseSpecification {

    def "logical filters should work"() {

        given:
            def filter = new And(new Or(new DummyFilter(false), new DummyFilter(false)), new Not(new DummyFilter(false)))
            def filter2 = new Or(new Not(new DummyFilter(true)), new Or(new DummyFilter(false), new DummyFilter(true)))

        expect:
            !filter.accept("")
            filter2.accept("")
    }


    class DummyFilter implements InterceptFilter<String> {
        private boolean returnValue

        DummyFilter(Boolean returnValue) {
            this.returnValue = returnValue
        }

        @Override
        boolean accept(String s) {
            return returnValue
        }

        @Override
        String description() {
            return "DummyAcceptor()"
        }
    }
}
