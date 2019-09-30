package pl.wendigo.chrome.driver.intercept.screencast

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import pl.wendigo.chrome.api.page.ScreencastFrameAckRequest
import pl.wendigo.chrome.api.page.StartScreencastRequest
import pl.wendigo.chrome.driver.SessionContext
import pl.wendigo.chrome.driver.dom.Node
import pl.wendigo.chrome.driver.intercept.InterceptFilter
import pl.wendigo.chrome.driver.intercept.Interceptor

class ScreenCast(
    val format: String = "jpeg",
    val quality : Int = 80,
    val width : Int = 1024,
    val height : Int = 768,
    val everyNthFrame : Int = 1
) : Interceptor<FrameId, Flowable<Frame>> {

    private val subject = BehaviorSubject.create<Frame>()
    private lateinit var subscription: Disposable

    override fun start(parentNode: Node, context: SessionContext, filter: InterceptFilter<FrameId>) {
        subscription = context.enablePage.flatMapPublisher {
            context.target.Page.startScreencast(StartScreencastRequest(
                format = format,
                quality = quality,
                maxWidth = height,
                maxHeight = height,
                everyNthFrame = everyNthFrame
            )).flatMapPublisher {
                context.target.Page.screencastFrame()
            }
        }
        .flatMapSingle { frame ->
            context.target.Page.screencastFrameAck(ScreencastFrameAckRequest(frame.sessionId)).map {
                frame
            }
        }
        .subscribeOn(Schedulers.io())
        .subscribe({ frame ->
            subject.onNext(Frame(
                    id = frame.sessionId,
                    data = frame.data,
                    timestamp = frame.metadata.timestamp ?: 0.0
                )
            )
        }, { e ->
            context.logger.warn("Caught exception when subscribing for screen cast: ${e.message}")
        })
    }

    override fun stop(parentNode: Node, context: SessionContext) {
        subscription.dispose()
        subject.onComplete()
    }

    override fun intercepted() : Flowable<Frame> = subject.toFlowable(BackpressureStrategy.DROP)
    override fun description() = "ScreenCast"
}