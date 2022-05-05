package sh.nemo.kjob

interface KJobFactory<Kj : KJob, KjConfig : KJob.Configuration> {
    fun create(configure: KjConfig.() -> Unit): KJob
}
