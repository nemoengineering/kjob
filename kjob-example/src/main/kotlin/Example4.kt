import sh.nemo.kjob.InMem
import sh.nemo.kjob.Job
import sh.nemo.kjob.kjob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

object MyFirstDelayedJob : Job("my-first-delayed-job") {
    val index = integer("index")
}

@ExperimentalTime
fun main() = runBlocking {
    val kjob = kjob(InMem).start()

    kjob.register(MyFirstDelayedJob) {
        execute {
            println("[${props[it.index]}] Hello delayed World!")
        }
    }

    kjob.schedule(MyFirstDelayedJob, 4.seconds) {
        props[it.index] = 1
    }
    kjob.schedule(MyFirstDelayedJob, 2.seconds) {
        props[it.index] = 2
    }
    kjob.schedule(MyFirstDelayedJob, 1.seconds) {
        props[it.index] = 3
    }
    kjob.schedule(MyFirstDelayedJob, 3.seconds) {
        props[it.index] = 4
    }

    // The job will print 'Hello delayed World' in 2 seconds on the console

    delay(5000) // This is just to prevent a premature shutdown

    kjob.shutdown()
}
