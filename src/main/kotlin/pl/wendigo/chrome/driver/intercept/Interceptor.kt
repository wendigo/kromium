package pl.wendigo.chrome.driver.intercept

import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.dom.Node

interface Interceptor<out Input, out Output> {
    fun start(parentNode : Node, context: SessionContext, filter : InterceptFilter<Input>)
    fun stop(parentNode : Node, context: SessionContext)
    fun intercepted() : Output
    fun description() : String
}

typealias InterceptorBlock<I, T> = Node.(I) -> T

interface InterceptFilter<in Input> {
    fun accept(input : Input) : Boolean
    fun description() : String
}

class And<in Input> (private vararg val filters : InterceptFilter<Input>) : InterceptFilter<Input> {
    override fun accept(input: Input): Boolean = filters.all {
        it.accept(input)
    }

    override fun description() = "(${filters.joinToString(separator = " && ", transform = { it.description() })})"
}

class Or<in Input> (private vararg val filters : InterceptFilter<Input>) : InterceptFilter<Input> {
    override fun accept(input: Input): Boolean = filters.any {
        it.accept(input)
    }

    override fun description() = "(${filters.joinToString(separator = " || ", transform = { it.description() })})"
}

class Not<in Input> (private val filter : InterceptFilter<Input>) : InterceptFilter<Input> {
    override fun accept(input: Input): Boolean = !filter.accept(input)

    override fun description() : String = "~${filter.description()}"
}