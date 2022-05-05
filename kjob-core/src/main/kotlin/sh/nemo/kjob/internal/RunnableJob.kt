package sh.nemo.kjob.internal

import sh.nemo.kjob.BaseJob
import sh.nemo.kjob.dsl.JobContext
import sh.nemo.kjob.job.JobExecutionType

interface RunnableJob {

    val job: BaseJob

    val executionType: JobExecutionType

    val maxRetries: Int

    suspend fun execute(context: JobContext<*>): JobResult
}
