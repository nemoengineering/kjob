package sh.nemo.kjob.dsl

import sh.nemo.kjob.job.ScheduledJob
import org.slf4j.Logger
import java.time.Duration

@JobDslMarker
class CompletionJobContext internal constructor(private val scheduledJob: ScheduledJob, val logger: Logger) {
    val jobName = scheduledJob.settings.name
    val jobId = scheduledJob.settings.id
    fun time(): Duration = Duration.between(scheduledJob.progress.startedAt, scheduledJob.progress.completedAt)
}
