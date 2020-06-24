package astify.util

typealias HCons<A, B> = HList.Cons<A, B>

sealed class HList {
    data class Cons<out A, out B: HList>(val value: A, val rest: B): HList()
    object Empty: HList()
}

fun main() {
    val a = HCons(5, HCons("hello", HList.Empty))
    val i: Int = a.value
    val s: String = a.rest.value
    val r: HList.Empty = a.rest.rest
}
