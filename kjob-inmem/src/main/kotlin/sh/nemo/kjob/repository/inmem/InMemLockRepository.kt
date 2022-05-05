package sh.nemo.kjob.repository.inmem

import sh.nemo.kjob.InMemKJob
import sh.nemo.kjob.job.Lock
import sh.nemo.kjob.repository.LockRepository
import java.time.Clock
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class InMemLockRepository(private val conf: InMemKJob.Configuration, private val clock: Clock) : LockRepository {

    constructor(clock: Clock, conf: InMemKJob.Configuration.() -> Unit)
            : this(InMemKJob.Configuration().also(conf), clock)

    private val map = ConcurrentHashMap<UUID, Lock>()

    override suspend fun ping(id: UUID): Lock {
        val lock = Lock(id, Instant.now(clock))
        map[id] = lock
        return lock
    }

    override suspend fun exists(id: UUID): Boolean {
        val lock = map[id]
        return lock == null || lock.updatedAt.plusSeconds(conf.expireLockInMinutes * 60).isAfter(Instant.now(clock))
    }

    internal fun deleteAll() {
        map.clear()
    }
}
