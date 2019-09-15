package pl.wendigo.chrome.driver

import org.slf4j.Logger

fun Logger.timedInfo(time : Long, message : String) {
    this.info("@$time: $message")
}

fun Logger.timedWarn(time : Long, message : String) {
    this.warn("@$time: $message")
}