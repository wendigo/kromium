package pl.wendigo.chrome.driver.intercept

import java.util.concurrent.atomic.AtomicBoolean

class FirstMatchingOnly<in Input> (private val filter : InterceptFilter<Input>) : InterceptFilter<Input> {

    private val matched = AtomicBoolean(false)

    override fun accept(input: Input): Boolean {
        if (matched.get()) {
            return false
        }

        if (filter.accept(input)) {
            if (matched.compareAndSet(false, true)) {
                return true
            }
        }

        return false
    }

    override fun description(): String {
        return "FirstMatchingOnly(${filter.description()})"
    }
}