class Pipeline {
    private val stages = mutableListOf<Pair<String, (List<String>) -> List<String>>>()

    fun addStage(name: String, transform: (List<String>) -> List<String>) {
        stages.add(name to transform)
    }

    fun execute(input: List<String>): List<String> {
        var result = input
        for ((_, transform) in stages) {
            result = transform(result)
        }
        return result
    }

    fun describe() {
        println("Pipeline stages :")
        stages.forEachIndexed { index, (name, _) ->
            println("${index + 1}. $name")
        }
    }


    fun compose(name1: String, name2: String, newName: String) {
        val stage1 = stages.find { it.first == name1 }?.second
        val stage2 = stages.find { it.first == name2 }?.second

        if (stage1 != null && stage2 != null) {
            val combined: (List<String>) -> List<String> = { input ->
                stage2(stage1(input))
            }
            addStage(newName, combined)
        }
    }

    fun fork(p1: Pipeline, p2: Pipeline, input: List<String>): Pair<List<String>, List<String>> {
        return p1.execute(input) to p2.execute(input)
    }
}

fun buildPipeline(block: Pipeline.() -> Unit): Pipeline {
    val pipeline = Pipeline()
    pipeline.block()
    return pipeline
}

fun main() {
    val logs = listOf(
        " INFO : server started ",
        " ERROR : disk full ",
        " DEBUG : checking config ",
        " ERROR : out of memory ",
        " INFO : request received ",
        " ERROR : connection timeout "
    )

    val logPipeline = buildPipeline {
        addStage("Trim") { list ->
            list.map { it.trim() }
        }

        addStage("Filter errors") { list ->
            list.filter { it.contains("ERROR", ignoreCase = true) }
        }

        addStage("Uppercase") { list ->
            list.map { it.uppercase() }
        }

        addStage("Add index") { list ->
            list.mapIndexed { index, s -> "${index + 1}. $s" }
        }
    }

    logPipeline.describe()

    val result = logPipeline.execute(logs)
    println("Result :")
    result.forEach { println(it) }

    println("\n--- Challenge: Compose ---")
    val simplePipeline = buildPipeline {
        addStage("Trim") { it.map { s -> s.trim() } }
        addStage("Uppercase") { it.map { s -> s.uppercase() } }
        compose("Trim", "Uppercase", "TrimAndUpper")
    }
    simplePipeline.describe()
}