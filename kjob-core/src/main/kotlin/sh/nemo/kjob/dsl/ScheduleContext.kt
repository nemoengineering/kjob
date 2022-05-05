package sh.nemo.kjob.dsl

import sh.nemo.kjob.Job
import sh.nemo.kjob.Prop
import java.util.*

@JobDslMarker
class ScheduleContext<J : Job> {
    /**
     * Override the default unique id generated for this job. Using the same id twice
     * will result in an exception on scheduling. It is recommended to define this to prevent
     * the execution of the same job more than once.
     */
    var jobId: String = UUID.randomUUID().toString()

    inner class Props {
        internal val props = mutableMapOf<String, Any>()

        operator fun <T> set(key: Prop<J, T>, value: T?) {
            if (value != null)
                props[key.name] = value as Any
        }
    }

    val props = Props()
}
