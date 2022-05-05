package sh.nemo.kjob.repository

import sh.nemo.kjob.job.JobSettings
import sh.nemo.kjob.job.JobStatus
import sh.nemo.kjob.job.ScheduledJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.singleOrNull
import java.time.Instant
import java.util.*

interface JobRepository {

    suspend fun exist(jobId: String): Boolean

    suspend fun save(jobSettings: JobSettings, runAt: Instant?): ScheduledJob

    suspend fun get(id: String): ScheduledJob?

    suspend fun update(id: String, oldKjobId: UUID?, kjobId: UUID?, status: JobStatus, statusMessage: String?, retries: Int): Boolean

    suspend fun reset(id: String, oldKjobId: UUID?): Boolean

    suspend fun startProgress(id: String): Boolean

    suspend fun completeProgress(id: String): Boolean

    suspend fun stepProgress(id: String, step: Long = 1): Boolean

    suspend fun setProgressMax(id: String, max: Long): Boolean

    suspend fun findNext(names: Set<String>, status: Set<JobStatus>, limit: Int): Flow<ScheduledJob>

    suspend fun findNextOne(names: Set<String>, status: Set<JobStatus>): ScheduledJob? =
            findNext(names, status, 1).singleOrNull()
}
