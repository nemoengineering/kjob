package sh.nemo.kjob.job

data class JobSettings(val id: String,
                       val name: String,
                       val properties: Map<String, Any>)
