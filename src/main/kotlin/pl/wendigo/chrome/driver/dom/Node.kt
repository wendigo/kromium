package pl.wendigo.chrome.driver.dom

import io.reactivex.Single
import pl.wendigo.chrome.api.dom.BoxModel
import pl.wendigo.chrome.api.dom.DiscardSearchResultsRequest
import pl.wendigo.chrome.api.dom.FocusRequest
import pl.wendigo.chrome.api.dom.GetAttributesRequest
import pl.wendigo.chrome.api.dom.GetAttributesResponse
import pl.wendigo.chrome.api.dom.GetBoxModelRequest
import pl.wendigo.chrome.api.dom.GetBoxModelResponse
import pl.wendigo.chrome.api.dom.GetSearchResultsRequest
import pl.wendigo.chrome.api.dom.NodeId
import pl.wendigo.chrome.api.dom.PerformSearchRequest
import pl.wendigo.chrome.api.dom.QuerySelectorAllRequest
import pl.wendigo.chrome.api.dom.QuerySelectorRequest
import pl.wendigo.chrome.api.dom.RequestChildNodesRequest
import pl.wendigo.chrome.api.dom.ResolveNodeRequest
import pl.wendigo.chrome.api.input.DispatchMouseEventRequest
import pl.wendigo.chrome.api.runtime.CallFunctionOnRequest
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.dom.input.Keys
import pl.wendigo.chrome.driver.intercept.InterceptFilter
import pl.wendigo.chrome.driver.intercept.Interceptor
import pl.wendigo.chrome.driver.intercept.InterceptorBlock
import pl.wendigo.chrome.driver.session.SessionClosedException
import pl.wendigo.chrome.driver.timedInfo
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

open class Node(
    private val parentNode: Node?,
    private val selector: String,
    private val nodeId: NodeId,
    private val context : SessionContext
) {
    /**
     * Returns node Id
     */
    open fun nodeId() = nodeId

    /**
     * Returns root document for given node
     */
    open fun document() : Document {
        var current : Node? = this

        while (current != null) {

            if (current is Document) {
                return current
            }

            current = current.parentNode
        }

        throw IllegalStateException("Could not find root document while traversing node hierarchy")
    }

    /**
     * Returns session in which it node was created.
     */
    fun session() = context.session

    /**
     * Returns path (how this node was queried).
     */
    open fun path() : String {
        var current : Node? = this
        val path = mutableListOf<String>()

        while (current != null) {
            path.add(current.selector + "(${current.nodeId()})")
            current = current.parentNode
        }

        return path.reversed().joinToString(" > ")
    }

    /**
     * Returns node selector (how this node can be retrieved once again)
     */
    open fun selector() = selector

    /**
     * Query node for a given selector returning first matching node.
     */
    fun query(selector : String) : Node {
        val nodeId = queryNode(selector)

        if (nodeId == 0) {
            throw NoSuchElementException("Node not found for selector $selector and parent node ${nodeId()}")
        }

        return constructNode(nodeId, selector)
    }

    /**
     * Query node for a given selector returning all matching nodes.
     */
    fun queryAll(selector : String) : List<Node> {
        return queryNodes(selector).map { node ->
            constructNode(node, selector)
        }
    }

    /**
     * Query node for a given xpath expression returning first matching node.
     */
    fun xpath(expression : String) : Node {
        val list = queryXpathNodes(expression).map {
            constructNode(it, expression)
        }

        return when (list.isEmpty()) {
            true -> throw NoSuchElementException("Node not found for selector $expression and parent node ${nodeId()}")
            else -> list.first()
        }
    }

    /**
     * Query nodes for a given xpath expression returning all matching nodes.
     */
    fun xpathAll(expression : String) : List<Node> {
        return queryXpathNodes(expression).map {
            constructNode(it, expression)
        }
    }

    /**
     * Retrieves children to given depth
     */
    fun children(depth : Int = 1) : List<Node> {
        context.enableDOM.flatMap {
            api.DOM.requestChildNodes(RequestChildNodesRequest(depth=depth, nodeId = nodeId))
        }.blockingGet()

        return api.DOM.setChildNodes().filter {
            it.parentId == nodeId
        }.map { event ->
            event.nodes.mapIndexed { index, (nodeId) ->
                constructNode(nodeId, "$selector:nth-child($index)")
            }
        }.blockingFirst()
    }

    /**
     * Returns node tagName (lower cased).
     */
    fun tagName() : String {
        return domAttribute(nodeId(), "this.tagName")
                .cast(String::class.java)
                .blockingGet()
                .toLowerCase()
    }

    /**
     * Returns node innerText value.
     */
    fun innerText() : String {
        return domAttribute(nodeId(), "this.innerText")
                .cast(String::class.java)
                .blockingGet()
    }

    /**
     * Returns node textContent value.
     */
    fun textContent() : String {
        return domAttribute(nodeId(), "this.textContent")
                .cast(String::class.java)
                .blockingGet()
    }

    /**
     * Returns node innerHTML value.
     */
    fun innerHtml() : String {
        return domAttribute(nodeId(), "this.innerHTML")
            .cast(String::class.java)
            .blockingGet()
    }

    /**
     * Returns node outerHTML value.
     */
    fun outerHtml() : String {
        return domAttribute(nodeId(), "this.outerHTML")
                .cast(String::class.java)
                .blockingGet()
    }

    /**
     * Returns node attribute value.
     */
    fun attribute(name : String) : String {
        return domAttribute(nodeId, "this.getAttribute('$name')")
                .cast(String::class.java)
                .blockingGet()
    }

    /**
     * Returns node attributes
     */
    fun attributes() : Map<String, String> {
        return api.DOM.getAttributes(GetAttributesRequest(nodeId))
            .map(GetAttributesResponse::attributes)
            .map { list ->
                val attributes = mutableMapOf<String, String>()

                for (i in 0 until (list.size / 2)) {
                    attributes.put(list[i*2], list[i*2+1])
                }

                attributes
            }.blockingGet()
    }

    /**
     * Returns node position on screen (center point of element relative to viewport).
     */
    fun position() : Position {
        return api.DOM.getBoxModel(GetBoxModelRequest(nodeId))
                .map(GetBoxModelResponse::model)
                .map { Position.fromBoxModel(it) }
                .blockingGet()
    }

    /**
     * Returns node boxModel.
     */
    fun boxModel() : BoxModel {
        return api.DOM.getBoxModel(GetBoxModelRequest(nodeId))
                .map(GetBoxModelResponse::model)
                .blockingGet()
    }

    /**
     * Scrolls to node to ensure it's visible in viewport.
     */
    fun scrollTo(force : Boolean = false) : Node {
        val function = when (force) {
            true -> "scrollIntoView"
            else -> "scrollIntoViewIfNeeded"
        }

        context.enableDOM
            .flatMap { api.DOM.resolveNode(ResolveNodeRequest(nodeId)) }
            .flatMap {
                api.Runtime.callFunctionOn(CallFunctionOnRequest(
                        objectId = it._object.objectId!!,
                        functionDeclaration = "function() { this.$function(); }"
                )).map {
                    true
                }
            }.blockingGet()

        return this
    }

    /**
     * Returns node value.
     */
    fun value() : String {
        return domAttribute(nodeId(), "this.value")
            .cast(String::class.java)
            .blockingGet()
    }

    /**
     * Focuses on element.
     */
    fun focus() : Node {
        api.DOM.focus(FocusRequest(nodeId)).blockingGet()
        return this
    }

    /**
     * Types chars :)
     */
    fun type(interval : Long = DEFAULT_TYPE_INTERVAL, vararg inputs : CharArray) : Node {
        inputs.forEach { input ->
            Keys.encode(input).forEach { keyEvent ->
                api.Input.dispatchKeyEvent(keyEvent)
                        .delay(interval, TimeUnit.MILLISECONDS)
                        .blockingGet()
            }
        }

        return this
    }

    /**
     * Types chars with default interval between keystrokes
     */
    fun type(vararg inputs: CharArray) = type(DEFAULT_TYPE_INTERVAL, *inputs)

    /**
     * Types strings with default interval between keystrokes
     */
    fun type(vararg inputs : String) : Node {
        return type(*inputs.map {
            it.toCharArray()
        }.toTypedArray())
    }

    /**
     * Types strings
     */
    fun type(interval : Long = DEFAULT_TYPE_INTERVAL, vararg inputs : String) : Node {
        return type(interval, *inputs.map {
            it.toCharArray()
        }.toTypedArray())
    }

    /**
     * Checks if element has focus.
     */
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val hasFocus : CheckCondition = object : CheckCondition {
        override fun check() : Boolean {
            return domAttribute(nodeId, "this == document.activeElement")
                    .cast(java.lang.Boolean::class.java)
                    .blockingGet()
                    .booleanValue()
        }

        override fun description() = "hasFocus(${this@Node})"
    }

    /**
     * Returns true if node is visible (drawn).
     */
    val isVisible : CheckCondition = object : CheckCondition {
        override fun check() : Boolean {
            return api.DOM.getBoxModel(GetBoxModelRequest(nodeId)).map {
                true
            }
            .onErrorReturnItem(false)
            .blockingGet()
        }

        override fun description() = "isVisible(${this@Node})"
    }

    /**
     * Returns true if element is enabled.
     */
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val isEnabled : CheckCondition = object : CheckCondition {
        override fun check() : Boolean {
            return domAttribute(nodeId(), "this.disabled")
                .map {
                    when (it) {
                        is java.lang.Boolean -> !it.booleanValue()
                        else -> false
                    }
                }
                .onErrorReturnItem(false)
                .blockingGet()
        }

        override fun description() = "isEnabled(${this@Node})"
    }

    /**
     * Returns true if element is selected.
     */
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val isSelected : CheckCondition = object : CheckCondition {
        override fun check() : Boolean {
            return domAttribute(nodeId(), "this.selected")
                .map {
                    when (it) {
                        is java.lang.Boolean -> it.booleanValue()
                        else -> false
                    }
                }
                .onErrorReturnItem(false)
                .blockingGet()
        }

        override fun description() = "isSelected(${this@Node})"
    }

    /**
     * Clicks on element (scrolling if necessary).
     */
    fun click(interval : Long = 5, native : Boolean = true) : Boolean {
        val position = scrollTo()
                .position()

        if (native) {
            val mousePressed = DispatchMouseEventRequest(
                    type = "mousePressed",
                    x = position.left.toDouble(),
                    y = position.top.toDouble(),
                    button = "left",
                    clickCount = 1
            )

            val mouseReleased = mousePressed.copy(type = "mouseReleased")

            api.Input.dispatchMouseEvent(mousePressed).blockingGet()

            return api.Input.dispatchMouseEvent(mouseReleased)
                    .delay(interval, TimeUnit.MILLISECONDS)
                    .map { true }
                    .blockingGet()
        } else {
            return this.domAttribute(nodeId(), "!this.click()").map { true }.blockingGet()
        }
    }

    /**
     * Allows to run more sophisticated conditions on node.
     */
    fun condition(lambda: Node.() -> Boolean) : CheckCondition {
        return object : CheckCondition {
            override fun check() = lambda.invoke(this@Node)
            override fun description() = "condition($lambda)"
        }
    }

    /**
     * Reverses condition
     */
    fun not(condition : CheckCondition) : CheckCondition {
        return object : CheckCondition {
            override fun check() = !condition.invoke()
            override fun description() = "~${condition.description()}"
        }
    }

    /**
     * Checks if all conditions hold (AND)
     */
    fun and(vararg conditions : CheckCondition) : CheckCondition {
        return object : CheckCondition {
            override fun check() = conditions.all {
                it.invoke()
            }

            override fun description() = "(${conditions.joinToString(" && ") { it.description() }})"
        }
    }

    /**
     * Checks if any condition hold (AND)
     */
    fun or(vararg conditions : CheckCondition) : CheckCondition {
        return object : CheckCondition {
            override fun check() = conditions.any {
                it.invoke()
            }

            override fun description() = "(${conditions.joinToString(" || ") { it.description() }})"
        }
    }

    /**
     * Checks if all given condition hold.
     */

    fun check(vararg conditions : CheckCondition) = try {
        conditions.all {
            it.check()
        }
    } catch (_ : Throwable) {
        false
    }

    /**
     * Checks if given selector is visible within a node
     */
    fun selectorIsVisible(selector : String) : CheckCondition {
        return selectorCondition(selector, {
            isVisible()
        })
    }

    /**
     * Checks if given selector exists (can be queried)
     */
    fun selectorExists(selector : String) : CheckCondition {
        return selectorCondition(selector, {
            true
        })
    }

    /**
     * Checks if given selector has focus
     */
    fun selectorHasFocus(selector : String) : CheckCondition {
        return selectorCondition(selector, {
            hasFocus()
        })
    }

    /**
     * Checks if selector fulfill condition
     */
    fun selectorCondition(selector : String, lambda: Node.() -> Boolean) : CheckCondition {
        return object : CheckCondition {
            override fun check(): Boolean {
                return try {
                    lambda.invoke(query(selector))
                } catch (e : Exception) {
                    false
                }
            }

            override fun description(): String {
                return "selectorCondition($selector, $lambda)"
            }
        }
    }

    /**
     * Waits for a conditions to hold.
     */
    fun await(
        timeout : Long = DEFAULT_AWAIT_INTERVAL,
        poolingInterval : Long = DEFAULT_AWAIT_POOLING,
        vararg conditions: CheckCondition
    ) : Node {

        val retries = (timeout / poolingInterval)
        val start = System.currentTimeMillis()

        for (i in 1..retries) {

            val currentTime = System.currentTimeMillis()
            val nextTarget = (poolingInterval * i)
            val sleepTime = start - currentTime + nextTarget

            if (session().isClosed()) {
                throw SessionClosedException("Session is already closed, cannot invoke operation")
            }

            if (i > 0 && sleepTime > 0) {
                Thread.sleep(sleepTime)
            }

            val fulfilled = check(*conditions)

            logger.timedInfo(currentTime, "[${String.format("% 3d", i * 100 / retries)}% of ${String.format("%-5d", timeout)} ms] Conditions ${conditions.map { it.description() }} ${when (fulfilled) { true -> "holding" else -> "not satisfied"}}")

            if (fulfilled) {
                return this
            }
        }

        logger.timedInfo(System.currentTimeMillis(), "Conditions ${conditions.map { it.description() }} timed out")

        throw TimeoutException("Conditions were not satisfied ${conditions.map { it.description() }}")
    }

    /**
     * Try await returns true if conditions were met or false if timeout occurred
     */
    fun tryAwait(timeout : Long = DEFAULT_AWAIT_INTERVAL,
                 poolingInterval : Long = DEFAULT_AWAIT_POOLING,
                 vararg conditions: CheckCondition) : Boolean = try {
        await(timeout, poolingInterval, *conditions)
        true
    } catch (_ : Exception) {
        false
    }

    /**
     * Waits for a conditions to hold with a pooling interval of 1/10th of timeout.
     */
    fun await(timeout : Long = DEFAULT_AWAIT_INTERVAL, vararg conditions: CheckCondition) =
        await(timeout, poolingForTimeout(timeout), *conditions)

    /**
     * Direct selector await.
     */
    fun await(timeout : Long = DEFAULT_AWAIT_INTERVAL, selector : String, lambda : Node.() -> Boolean) =
        await(timeout, selectorCondition(selector, lambda))

    /**
     * Await for selector and return it
     */
    fun awaitSelector(timeout : Long = DEFAULT_AWAIT_INTERVAL, selector: String, lambda : Node.() -> Boolean) =
        await(timeout, selectorCondition(selector, lambda)).run {
            query(selector)
        }

    /**
     * Takes screenshot of a node element.
     */
    open fun screenshot(location : String, format : String = "png", quality : Int = 80) : Node {
        val (layout, _, _) = api.Page.getLayoutMetrics().blockingGet()
        val box = boxModel()

        // Resize viewport to match element size
        document()
                .resizeViewport(box.width, box.height)

        // Scroll to element
        document().scrollBy(box.border[0], box.border[1])

        // Capture screenshot of the element
        document().captureScreenshot(location, format, quality)
                .resizeViewport(layout.clientWidth, layout.clientHeight)

        return this
    }

    /**
     * Runs interception of asynchronous events happening after interceptor starts and before it ends
     */
    fun <Input, Output, I> intercept(interceptor : I, filter : InterceptFilter<Input>, block : InterceptorBlock<I, Any>) : Output where I : Interceptor<Input, Output> {
        startInterceptor(interceptor, filter)
        block.invoke(this, interceptor)
        return stopInterceptor(interceptor)
    }

    fun <Input, Output, I> startInterceptor(interceptor : I, filter : InterceptFilter<Input>) : Node where I : Interceptor<Input, Output> {
        interceptor.start(this, context, filter)
        logger.timedInfo(System.currentTimeMillis(), "Started intercepting ${ interceptor.description() } with filters ${filter.description() }")
        return this
    }

    fun <Input, Output, I> startInterceptor(interceptor : I) : Node where I : Interceptor<Input, Output> {
        startInterceptor(interceptor, object : InterceptFilter<Input> {
            override fun accept(input: Input): Boolean = true
            override fun description() = "AlwaysAccepting"
        })

        return this
    }

    fun <Input, Output, I> stopInterceptor(interceptor : I) : Output where I : Interceptor<Input, Output> {
        interceptor.stop(this, context)

        logger.timedInfo(System.currentTimeMillis(), "Stopped intercepting ${interceptor.description()}")

        return interceptor.intercepted()
    }

    /**
     * Waits for a conditions to hold for one second.
     */
    fun await(vararg conditions: CheckCondition) = await(1000, 50, *conditions)

    private fun poolingForTimeout(timeout: Long) : Long {
        val interval = timeout / 50

        return when {
            interval < 50 -> 50
            interval > 100 -> 100
            else -> interval
        }
    }

    private fun queryNodes(selector : String) : List<NodeId> {
        for (i in (0 until 10)) {
            try {
                val nodeId = nodeId()

                return api.DOM.querySelectorAll(QuerySelectorAllRequest(nodeId=nodeId, selector = selector)).map {
                    it.nodeIds
                }
                .delay(10L * i, TimeUnit.MILLISECONDS)
                .blockingGet()
            } catch (e : Exception) {
                logger.warn("Could not queryNodes due to $e")
            }
        }

        return listOf()
    }

    private fun queryNode(selector : String) : NodeId {
        for (i in (0 until 10)) {
            try {
                val nodeId = nodeId()

                return api.DOM.querySelector(QuerySelectorRequest(nodeId = nodeId, selector = selector)).map {
                    it.nodeId
                }
                .delay(10L * i, TimeUnit.MILLISECONDS)
                .blockingGet()
            } catch (e : Exception) {
                logger.warn("Could not queryNode due to $e")
            }
        }

        return 0
    }

    private fun queryXpathNodes(expression: String) : List<NodeId> {
        val (searchId, count) = api.DOM.performSearch(PerformSearchRequest(expression))
                .blockingGet()

        var nodeIds : List<NodeId> = mutableListOf()

        if (count > 0) {
            nodeIds += api.DOM.getSearchResults(GetSearchResultsRequest(
                    searchId = searchId,
                    fromIndex = 0,
                    toIndex = count
            )).blockingGet().nodeIds
        }

        api.DOM.discardSearchResults(DiscardSearchResultsRequest(searchId))
                .blockingGet()

        return nodeIds.toList()
    }

    /**
     * Constructs new node based on current one.
     */
    private fun constructNode(id : NodeId, selector : String) = Node(this, selector, id, context)

    private fun domAttribute(nodeId : NodeId, attribute : String) : Single<Any?> {
        return api.DOM.resolveNode(ResolveNodeRequest(nodeId)).flatMap {
            api.Runtime.callFunctionOn(CallFunctionOnRequest(
                    objectId = it._object.objectId!!,
                    functionDeclaration = "function() { return $attribute; }"
            )).flatMap {
                if (it.result.type == "undefined") {
                    Single.error(RuntimeException("Result was undefined"))
                } else {
                    Single.just(it.result.value!!)
                }
            }
        }
    }

    override fun toString() = "Node($nodeId)[${path()}]"

    internal val api by lazy {
        context.target
    }

    val logger by lazy {
        context.logger
    }

    companion object {
        const val DEFAULT_TYPE_INTERVAL = 5L
        const val DEFAULT_AWAIT_INTERVAL = 1000L
        const val DEFAULT_AWAIT_POOLING = 50L
    }
}