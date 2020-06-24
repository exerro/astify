package astify.util

fun <T> Sequence<T?>.firstNotNull(): T? = filter { it != null } .firstOrNull()
