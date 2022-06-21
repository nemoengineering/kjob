package sh.nemo.kjob.repository.mongo

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import io.kotest.provided.ProjectConfig
import sh.nemo.kjob.repository.JobRepository
import sh.nemo.kjob.repository.JobRepositoryContract
import sh.nemo.kjob.repository.mongo.structure.JobSettingsStructure
import sh.nemo.kjob.repository.mongo.structure.ScheduledJobStructure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId

@ExperimentalCoroutinesApi
class MongoJobRepositorySpec : JobRepositoryContract() {
    private val mongoClient = ProjectConfig.newMongoClient()

    override val testee: JobRepository = MongoJobRepository(mongoClient, clock) {
        databaseName = "test-" + id()
        client = mongoClient
    }

    private val mongoTestee = testee as MongoJobRepository

    override suspend fun deleteAll() {
        mongoTestee.deleteAll()
    }

    override fun randomJobId(): String = ObjectId.get().toHexString()

    override suspend fun beforeSpec(spec: Spec) {
        mongoTestee.ensureIndexes()
    }

    init {
        should("ensure index") {
            val unique = mongoClient
                    .getDatabase(mongoTestee.conf.databaseName)
                    .getCollection(mongoTestee.conf.jobCollection)
                    .listIndexes()
                    .asFlow()
                    .first { it.getString("name") == "unique_job_id" }
            unique.getBoolean("unique") shouldBe true
            val expectedKey = Document().append("${ScheduledJobStructure.SETTINGS.key}.${JobSettingsStructure.ID.key}", 1)
            unique.get("key", Document::javaClass) shouldBe expectedKey
        }

    }

}
