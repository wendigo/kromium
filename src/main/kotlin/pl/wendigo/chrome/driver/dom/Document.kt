package pl.wendigo.chrome.driver.dom

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Timed
import pl.wendigo.chrome.api.dom.GetDocumentRequest
import pl.wendigo.chrome.api.dom.NodeId
import pl.wendigo.chrome.api.emulation.SetDeviceMetricsOverrideRequest
import pl.wendigo.chrome.api.page.CaptureScreenshotRequest
import pl.wendigo.chrome.api.page.FrameId
import pl.wendigo.chrome.api.page.NavigateRequest
import pl.wendigo.chrome.api.page.NavigateToHistoryEntryRequest
import pl.wendigo.chrome.api.page.NavigationEntry
import pl.wendigo.chrome.api.page.PrintToPDFRequest
import pl.wendigo.chrome.api.page.ReloadRequest
import pl.wendigo.chrome.api.page.SetDownloadBehaviorRequest
import pl.wendigo.chrome.api.runtime.EvaluateRequest
import pl.wendigo.chrome.api.runtime.RemoteObject
import pl.wendigo.chrome.driver.EvaluationFailed
import pl.wendigo.chrome.driver.Files
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.timedInfo
import pl.wendigo.chrome.driver.timedWarn
import java.io.Closeable
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class Document(
    frameId: FrameId,
    private val context: SessionContext
) : Node(
        null,
        "html",
        0,
        context
), Closeable {

    private val rootId = AtomicInteger(ROOT_DOCUMENT_NOT_LOADED)
    private val frameId = AtomicReference<FrameId>(frameId)

    @Volatile
    private var status = LoadingStatus.NAVIGATING

    @Volatile
    private var latestStatus : EventTimestamp = 0

    init {
        this.observeLoading()
    }

    private fun <T, U> runOnIncrementalTimestamp(value : Timed<T>, success : T.() -> U, discard : T.() -> U) : U? {
        var retval: U?

        synchronized(this) {
            if (value.time() >= latestStatus) {
                retval = success.invoke(value.value())
                latestStatus = value.time()
            } else {
                retval = discard.invoke(value.value())
            }
        }

        return retval
    }

    private fun startNavigating() {

        synchronized(this) {
            rootId.set(ROOT_DOCUMENT_NOT_LOADED)
            status = LoadingStatus.NAVIGATING

            if (frameId.get().isNotEmpty()) {
                logger.timedInfo(System.currentTimeMillis(), "Started navigating frame ${frameId.get()}, root document id is now $ROOT_DOCUMENT_NOT_LOADED")
            }
        }
    }

    private fun reloadRootId() {
        rootId.set(ROOT_DOCUMENT_NOT_LOADED)

        api.DOM.getDocument(GetDocumentRequest(depth = 0)).map { (root) ->
            root.nodeId
        }
        .subscribeOn(Schedulers.io())
        .subscribe( { newRootId ->
            synchronized(this) {

                if (newRootId > rootId.get()) {
                    val oldId = rootId.getAndSet(newRootId)
                    logger.timedInfo(System.currentTimeMillis(), "Reloaded root document id from $oldId to $newRootId")
                }
            }
        }, { error ->
            logger.timedWarn(System.currentTimeMillis(), "Could not reload document id due to ${error.message}")
        })
    }

    private fun observeLoading() {
        api.Page.frameNavigated()
            .timestamp()
            .observeOn(Schedulers.io())
            .filter { it.value().frame.id == frameId.get() }
            .subscribe { event ->
                synchronized(this) {
                    logger.timedInfo(event.time(), "Frame ${event.value().frame.id} navigated...")
                }
            }

        api.Page.frameStartedLoading()
            .timestamp()
            .observeOn(Schedulers.io())
            .filter { it.value().frameId == frameId.get() }
            .subscribe { event ->
                runOnIncrementalTimestamp(event, {
                    logger.timedInfo(event.time(), "Frame ${event.value().frameId} started loading, setting status to NAVIGATING")

                    startNavigating()
                    event.time()
                }, {
                    logger.timedInfo(event.time(), "Event ${event.value()} discarded, happened before last registered")
                })
            }

        api.Page.frameStoppedLoading()
            .timestamp()
            .observeOn(Schedulers.io())
            .filter { it.value().frameId == frameId.get() }
            .subscribe { event ->
                synchronized(this) {
                    logger.timedInfo(event.time(), "Frame ${event.value().frameId} stopped loading...")
                }
            }

        api.Page.lifecycleEvent()
            .timestamp()
            .observeOn(Schedulers.io())
            .doOnNext { event ->
                logger.timedInfo(event.time(), "Lifecycle event ${event.value().name} fired")
            }
            .filter {
                (it.value().name == "DOMContentLoaded" || it.value().name == "networkIdle").and(it.value().frameId.contentEquals(frameId.get()))
            }
            .subscribe { event ->
                runOnIncrementalTimestamp(event, {
                    when (event.value().name) {
                        "DOMContentLoaded" -> {
                            logger.timedInfo(event.time(), "Frame ${event.value().frameId} fired load event, setting status to LOADED")
                            status = LoadingStatus.LOADED
                            reloadRootId()
                        }
                        "networkIdle" -> {
                            logger.timedInfo(event.time(), "Frame ${event.value().frameId} fired networkIdle event, setting status to IDLE")
                            status = LoadingStatus.IDLE
                        }
                    }

                    event.time()
                }, {
                    logger.timedInfo(event.time(), "Event ${event.value()} discarded, happened before last registered")
                })
            }

        context.enableDOM.flatMapObservable {
            api.DOM.documentUpdated().timestamp().toObservable()
        }
        .filter {
            // Reload only while document is loaded
            status >= LoadingStatus.LOADED
        }
        .subscribe {
            reloadRootId()
        }
    }

    /**
     * Waits for frame to be loaded.
     */
    fun waitForLoad() : Document {
        logger.info("Waiting for document to load...")

        this.await(MAXIMUM_DOCUMENT_LOAD_TIME, documentLoaded())

        return this
    }

    /**
     * Navigates document to new url.
     */
    fun navigate(url : String) : Document {
        val currentLocation = location()

        if (url.removePrefix(currentLocation).startsWith("#")) {
            evaluate("(function() { return window.location.href = '$url'; })();")
        } else {
            logger.info("Navigating to $url from ${location()}...")

            startNavigating()

            val loadingFrameId = context.enableDomains.flatMap {
                api.Page.navigate(NavigateRequest(url = url))
            }.map { it.frameId }.blockingGet()

            frameId.set(loadingFrameId)
        }

        return this
    }

    /**
     * Returns document title.
     */
    fun title() : String = evaluate("(function() { return document.title; })();").value as String

    /**
     * Returns current document location.
     */
    fun location() = evaluate("(function() { return window.location.href; })();").value as String

    /**
     * Navigates document to new url and waits for it to load.
     */
    fun navigateAndWait(url : String) : Document {
        return navigate(url)
                .waitForLoad()
    }

    /**
     * Reload current document.
     */
    fun reload(ignoreCache : Boolean = false) : Document {
        startNavigating()

        context.enableDomains.flatMap {
            api.Page.reload(ReloadRequest(ignoreCache = ignoreCache))
        }.blockingGet()

        return this
    }

    /**
     * Scrolls to coordinates.
     */
    fun scrollBy(x : Double, y : Double) : Node {
        evaluate("(function() { window.scrollBy($x, $y); })();")
        return this
    }

    /**
     * Returns navigation history list.
     */
    fun history() : List<NavigationEntry> {
        return context.enableDomains.flatMap {
            api.Page.getNavigationHistory()
        }.map {
            it.entries
        }.blockingGet()
    }

    /**
     * Navigates back in history
     */
    fun navigateBack() : Document {
        startNavigating()

        context.enableDomains.flatMap {
            api.Page.getNavigationHistory()
        }.flatMap { (currentIndex, entries) ->
            if (currentIndex <= 0 || currentIndex > entries.size - 1) {
                Single.error(IllegalStateException("Could not navigate back in history, current index: $currentIndex, history length: ${entries.size}"))
            } else {
                api.Page.navigateToHistoryEntry(NavigateToHistoryEntryRequest(entries[currentIndex-1].id))
            }
        }.blockingGet()

        return this
    }

    /**
     * Navigates back in history
     */
    fun navigateForward() : Document {
        startNavigating()

        context.enableDomains.flatMap {
            api.Page.getNavigationHistory()
        }.flatMap { (currentIndex, entries) ->
            if (currentIndex < 0 || currentIndex >= entries.size - 1) {
                Single.error(IllegalStateException("Could not navigate forward in history, current index: $currentIndex, history length: ${entries.size}"))
            } else {
                api.Page.navigateToHistoryEntry(NavigateToHistoryEntryRequest(entries[currentIndex+1].id))
            }
        }.blockingGet()

        return this
    }

    /**
     * Evaluates Javascript expression in the context of loaded document and returns RemoteObject.
     */
    fun evaluate(expression : String) : RemoteObject {

        logger.info("Evaluating ${expression.replace("\n", "")}")

        val remoteObject = api.Runtime.evaluate(EvaluateRequest(expression = expression, returnByValue = true))
                .blockingGet()

        if (remoteObject.exceptionDetails != null) {
            throw EvaluationFailed(remoteObject.exceptionDetails?.exception?.description ?: remoteObject.exceptionDetails.toString())
        }

        return remoteObject.result
    }

    /**
     * Checks if document was fully loaded
     */
    fun documentLoaded() : CheckCondition {
        return object : CheckCondition {
            override fun check(): Boolean = (status >= LoadingStatus.LOADED && rootId.get() != ROOT_DOCUMENT_NOT_LOADED)
            override fun description() = "DocumentLoaded(${frameId.get()}) = (${status.name}, ${rootId.get()})"
        }
    }

    /**
     * Checks if network is idle
     */
    fun networkIdle() : CheckCondition {
        return object : CheckCondition {
            override fun check() = status == LoadingStatus.IDLE
            override fun description() = "NetworkIdle(${frameId.get()}) = ${status == LoadingStatus.IDLE}"
        }
    }

    /**
     * Returns document id
     */
    override fun nodeId() : NodeId {
        return rootNodeId().blockingGet()
    }

    /**
     * Takes screenshot of whole document.
     */
    override fun screenshot(location : String, format : String, quality : Int) : Document {
        val (layout, _, _) = api.Page.getLayoutMetrics().blockingGet()

        return resizeViewport(layout.clientWidth, layout.clientHeight)
                .captureScreenshot(location, format, quality)
    }

    /**
     * Takes full page screenshot.
     */
    fun fullpageScreenshot(location : String, format : String = "jpeg", quality : Int = 50) : Document {
        val (layout, _, content) = api.Page.getLayoutMetrics().blockingGet()

        return resizeViewport(content.width.toInt(), content.height.toInt())
            .captureScreenshot(location, format, quality)
            .resizeViewport(layout.clientWidth, layout.clientHeight)
    }

    internal fun captureScreenshot(location : String, format : String, quality : Int) : Document {
        context.enableDomains.flatMap {
            api.Page.captureScreenshot(CaptureScreenshotRequest(format = format, quality = quality))
        }.map { (data) ->
            Base64.getDecoder().decode(data)
        }.flatMap { image ->
            Files.writeFile(location, image)
        }.blockingGet()

        logger.timedInfo(System.currentTimeMillis(), "Captured screenshot to $location")

        return this
    }

    /**
     * Resizes viewport to given dimensions
     */
    fun resizeViewport(width : Int, height : Int) : Document {
        api.Emulation.setDeviceMetricsOverride(SetDeviceMetricsOverrideRequest(
                width = width,
                height = height,
                deviceScaleFactor = 1.0,
                mobile = false
        )).blockingGet()

        logger.timedInfo(System.currentTimeMillis(), "Resized viewport to width=$width, height=$height, scale=1.0")

        return this
    }

    /**
     * Prints document to PDF.
     */
    fun pdf(location : String) : Document {
        context.enableDomains.flatMap {
            api.Page.printToPDF(PrintToPDFRequest())
        }.map { (data) ->
            Base64.getDecoder().decode(data)
        }.flatMap { pdf ->
            Files.writeFile(location, pdf)
        }.blockingGet()

        return this
    }

    /**
     * Starts accepting files to download
     */
    fun downloadFilesTo(directory : String) : Document {
        context.enableDomains.flatMap {
            api.Page.setDownloadBehavior(SetDownloadBehaviorRequest(
                behavior = "allow",
                downloadPath = directory
            ))
        }.blockingGet()

        return this
    }

    private fun rootNodeId() : Single<NodeId> {
        return Single
            .defer { Single.just(rootId.get()) }
            .flatMap { value ->
                if (value != ROOT_DOCUMENT_NOT_LOADED) {
                    Single.just(value)
                } else {
                    Single.error(RuntimeException("Document root not yet loaded"))
                }
            }.retryWhen { errors ->
                errors.delay(10, TimeUnit.MILLISECONDS)
            }
    }

    /**
     * Closes document
     */
    override fun close() = session().close()
    override fun toString() = "Document(context=$context, rootId=$rootId, frameId=$frameId, status=$status, latestStatus=$latestStatus)"

    companion object {
        const val ROOT_DOCUMENT_NOT_LOADED = 0
        const val MAXIMUM_DOCUMENT_LOAD_TIME = 1000 * 60 * 3L // 3 minutes
    }
}