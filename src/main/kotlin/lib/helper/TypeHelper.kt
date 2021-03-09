package lib.helper

@Suppress("UNCHECKED_CAST")
fun <T> Any.cast(): T = this as T


data class TypedIndex<T>(val name: String)
operator fun <T> Map<String, Any?>.get(index: TypedIndex<T>): T {
    require(this.containsKey(index.name)) { "Index not found!" }
    return this[index.name]!!.cast()
}
fun <T> Map<String, Any?>.containsKey(index: TypedIndex<T>): Boolean {
    if(!this.containsKey(index.name)) return false
    return try {
        this[index]
        true
    } catch (e: Exception) {
        false
    }
}
infix fun <T> TypedIndex<T>.to(second: T): Pair<String, T> {
    return Pair(this.name, second)
}