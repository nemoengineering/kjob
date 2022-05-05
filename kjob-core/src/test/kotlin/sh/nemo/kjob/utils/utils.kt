package sh.nemo.kjob.utils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun CountDownLatch.waitSomeTime(millis: Long = 500): Boolean =
        await(millis, TimeUnit.MILLISECONDS)
