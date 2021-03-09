package system

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StateDelegate<V>(val name: String, initialValue: V, private val afterValueChange: ((V) -> Unit)? = null): ReadWriteProperty<Any?, V> {
    private var value = initialValue.also { afterValueChange?.invoke(initialValue) }

    public override fun getValue(thisRef: Any?, property: KProperty<*>): V { return value }
    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.value = value
        afterValueChange?.invoke(value)
    }
    public fun getValue(): V = value
}