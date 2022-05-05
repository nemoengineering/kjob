package sh.nemo.kjob.repository

import sh.nemo.kjob.job.Lock
import java.util.*

interface LockRepository {

    suspend fun ping(id: UUID): Lock

    suspend fun exists(id: UUID): Boolean
}
