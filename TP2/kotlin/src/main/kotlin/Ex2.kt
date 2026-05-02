
class Cache<K : Any, V : Any> {
    private val storage = mutableMapOf<K, V>()

    fun put(key: K, value: V) {
        storage[key] = value
    }

    fun get(key: K): V? = storage[key]

    fun evict(key: K) {
        storage.remove(key)
    }

    fun size(): Int = storage.size

    fun getOrPut(key: K, default: () -> V): V {
        val current = storage[key]
        return if (current != null) {
            current
        } else {
            val newValue = default()
            put(key, newValue)
            newValue
        }
    }

    fun transform(key: K, action: (V) -> V): Boolean {
        val currentValue = storage[key] ?: return false
        storage[key] = action(currentValue)
        return true
    }

    fun snapshot(): Map<K, V> = storage.toMap()

    fun filterValues(predicate: (V) -> Boolean): Map<K, V> {
        return storage.filterValues { predicate(it) }
    }
}

fun main() {
    println("--- Word frequency cache ---")
    val wordCache = Cache<String, Int>()
    wordCache.put("kotlin", 1)
    wordCache.put("scala", 1)
    wordCache.put("haskell", 1)

    println("Size : ${wordCache.size()}")
    println("Frequency of \"kotlin\": ${wordCache.get("kotlin")}")

    println("getOrPut \"kotlin\": ${wordCache.getOrPut("kotlin") { 0 }}")
    println("getOrPut \"java\": ${wordCache.getOrPut("java") { 0 }}")
    println("Size after getOrPut : ${wordCache.size()}")

    val transformedKotlin = wordCache.transform("kotlin") { it + 1 }
    println("Transform \"kotlin\" (+1) : $transformedKotlin")

    val transformedCobol = wordCache.transform("cobol") { it + 1 }
    println("Transform \"cobol\" (+1) : $transformedCobol")

    println("Snapshot : ${wordCache.snapshot()}")

    val activeWords = wordCache.filterValues { it > 0 }
    println("Active Words : $activeWords")

    println("\n--- Id registry cache ---")
    val idRegistry = Cache<Int, String>()
    idRegistry.put(1, "Alice")
    idRegistry.put(2, "Bob")

    println("Id 1 -> ${idRegistry.get(1)}")
    println("Id 2 -> ${idRegistry.get(2)}")

    idRegistry.evict(1)
    println("After evict id 1, size : ${idRegistry.size()}")
    println("Id 1 after evict -> ${idRegistry.get(1)}")
}