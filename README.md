# kjob

[![](https://jitpack.io/v/nemoengineering/kjob.svg)](https://jitpack.io/#nemoengineering/kjob)
[![CD/CI Workflow](https://github.com/nemoengineering/kjob/actions/workflows/cdci-workflow.yml/badge.svg)](https://github.com/nemoengineering/kjob/actions/workflows/cdci-workflow.yml)

A coroutine based persistent background (cron) scheduler written in Kotlin.

This codebase is a fork of https://github.com/justwrote/kjob

## Installation

This library is currently available through JitPack.

In order to use the library, add first the JitPack repository to the `repositories` block and then the dependency to
the `dependencies` block:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nemoengineering:kjob-core:<version>")
    implementation("com.github.nemoengineering:kjob-mongo:<version>") // for mongoDB persistence
    implementation("com.github.nemoengineering:kjob-inmem:<version>") // for in-memory 'persistence' (e.g. tests)
}
```

## Usage

```kotlin
import sh.nemo.kjob.Mongo
import sh.nemo.kjob.Job
import sh.nemo.kjob.job.JobExecutionType
import sh.nemo.kjob.kjob

object OrderCreatedEmail : Job("order-created-email") {
    val recipient = string("recipient")
}

// ...

// start kjob with mongoDB persistence and default configuration
val kjob = kjob(Mongo).start()

// ...

kjob.register(OrderCreatedEmail) {
    executionType = JobExecutionType.NON_BLOCKING // our fake email client is non blocking
    maxRetries = 3
    execute {
        val to = props[it.recipient] // getting address from customer
        client.sendTo(to, subject, body)
    }.onError {
        // errors will automatically logged but we might want to do some metrics or something 
    }
}

// ...

kjob.schedule(OrderCreatedEmail) {
    props[it.recipient] = "customer@example.com"
}

// or provide some delay for the scheduling
kjob.schedule(OrderCreatedEmail, 5.seconds) {
    props[it.recipient] = "customer@example.com"
}
// this runs the job not immediately but - you may guess it already - in 5 seconds!
```

For more details please take a look at
the [examples](https://github.com/nemoengineering/kjob/tree/main/kjob-example/src/main/kotlin).

## Starting kjob

Multiple schedulers are running in the background after starting kjob. There is one looking for new jobs every second
(period can be defined in the configuration). If a job has been found that has not yet been started (or reset after an
error)
and the kjob instance is currently not executing too many other jobs of the same kind (there are blocking and
non-blocking jobs)
kjob will process it. The second scheduler is handling the locking. It indirectly tells the other kjob instances that
this one is still alive. The last scheduler is cleaning up locked jobs of other not responding kjob instances to make
the jobs
available again for execution.

## Multiple kjob instances

To be fault-tolerant you sometimes want to have multiple instances of your job processor. This might be in the same app
or on
different nodes. Therefore, every kjob instances has a unique id which will be added to the job it is currently
executing.
This actually locks a job to a specific kjob instance. If the kjob instance is somehow dying while executing a job
another
kjob instance will remove the lock after a specific time (which can be defined in the configuration) and will pick it up
again.

## Changing Configuration

Changing the config is fairly easy. There is not another config file and everything will be done in code - so you can
use
your own configuration.

```kotlin
kjob(InMem) {
    nonBlockingMaxJobs = 10 // how many non-blocking jobs will be executed at max in parallel per instance
    blockingMaxJobs = 3 // same for blocking jobs
    maxRetries = 5 // how often will a job be retried until it fails
    defaultJobExecutor = JobExecutionType.BLOCKING // default job execution type

    exceptionHandler =
        { t: Throwable -> logger.error("Unhandled exception", t) } // default error handler for coroutines
    keepAliveExecutionPeriodInSeconds = 60 // the time between 'I am alive' notifications
    jobExecutionPeriodInSeconds = 1 // the time between new job executions
    cleanupPeriodInSeconds = 300 // the time between job clean-ups
    cleanupSize = 50 // the amount of jobs that will be cleaned up per schedule
}.start()
```

## MongoDB Configuration

Despite the configuration above there are some mongoDB specific settings that will be shown next.

```kotlin
kjob(Mongo) {
    // all the config above plus those:
    connectionString = "mongodb://localhost" // the mongoDB specific connection string 
    client = null // if a client is specified the 'connectionString' will be ignored
    databaseName = "kjob" // the database where the collections below will be created
    jobCollection = "kjob-jobs" // the collection for all jobs
    lockCollection = "kjob-locks" // the collection for the locking
    expireLockInMinutes = 5L // using the TTL feature of mongoDB to expire a lock
}.start()
```

## Extensions

If you want to add new features to kjob you can do so with a kjob extension.

```kotlin
object ShowIdExtension : ExtensionId<ShowIdEx>

class ShowIdEx(
    private val config: Configuration,
    private val kjobConfig: BaseKJob.Configuration,
    private val kjob: BaseKJob<BaseKJob.Configuration>
) : BaseExtension(ShowIdExtension) {
    class Configuration : BaseExtension.Configuration()

    fun showId() {
        // here you have access to some internal properties
        println("KJob has the following id: ${kjob.id}")
    }
}

object ShowIdModule :
    ExtensionModule<ShowIdEx, ShowIdEx.Configuration, BaseKJob<BaseKJob.Configuration>, BaseKJob.Configuration> {
    override val id: ExtensionId<ShowIdEx> = ShowIdExtension
    override fun create(
        configure: ShowIdEx.Configuration.() -> Unit,
        kjobConfig: BaseKJob.Configuration
    ): (BaseKJob<BaseKJob.Configuration>) -> ShowIdEx {
        return { ShowIdEx(ShowIdEx.Configuration().apply(configure), kjobConfig, it) }
    }
}

// ...

val kjob = kjob(InMem) {
    extension(ShowIdModule) // register our extension and bind it to the kjob life cycle
}

// ...

kjob(ShowIdExtension).showId() // access our new extension method
```

To see a more advanced version take a look at
this [example](https://github.com/nemoengineering/kjob/blob/main/kjob-example/src/main/kotlin/Example_Extension.kt).

## Cron

With kjob you are also able to schedule jobs with the familiar cron expression. To get Kron - the name of the extension
to enable Cron scheduling in kjob - you need to add the following dependency:

```kotlin
dependencies {
    implementation("com.github.nemoengineering:kjob-kron:<version>")
}
``` 

After that you can schedule cron jobs as easy as every other job with kjob.

```kotlin
// define a Kron job with a name and a cron expression (e.g. 5 seconds)
object PrintStuff : KronJob("print-stuff", "*/5 * * ? * * *")

// ...

val kjob = kjob(InMem) {
    extension(KronModule) // enable the Kron extension
}

// ...

// define the executed code
kjob(Kron).kron(PrintStuff) {
    maxRetries = 3 // and you can access the already familiar settings you are used to
    execute {
        println("${Instant.now()}: executing kron task '${it.name}' with jobId '$jobId'")
    }
}
```

You can find more in
this [example](https://github.com/nemoengineering/kjob/blob/main/kjob-example/src/main/kotlin/Example_Kron.kt).

## License

kjob is licensed under the [Apache 2.0 License](https://github.com/nemoengineering/kjob/blob/main/LICENSE).
