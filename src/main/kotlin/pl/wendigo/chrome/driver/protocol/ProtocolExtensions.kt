package pl.wendigo.chrome.driver.protocol

import pl.wendigo.chrome.api.network.MonotonicTime

fun MonotonicTime.toLong() = (this * 1_000_000).toLong()