package org.ratelog.tmdb

import java.util.concurrent.Semaphore
import kotlin.math.max

class TmdbRateLimiter(private val maxRequestsPerSecond: Int) {
    private val semaphore = Semaphore(maxRequestsPerSecond)
    private val windowMillis = 1000L

    init {
        Thread.ofVirtual().start {
            while (!Thread.interrupted()) {
                Thread.sleep(windowMillis)
                semaphore.release(maxRequestsPerSecond - semaphore.availablePermits())
            }
        }
    }

    fun acquire() {
        semaphore.acquire()
        val delay = max(0L, windowMillis / maxRequestsPerSecond - 1)
        Thread.sleep(delay)
    }
}
