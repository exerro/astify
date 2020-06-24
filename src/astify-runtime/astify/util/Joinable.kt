package astify.util

interface Joinable<T, J> {
    fun join(value: T): J
}

//////////////////////////////////////////////////////////////////////////////////////////
// example:

class J0<T>: Joinable<T, J1<T>> {
    override fun join(value: T): J1<T> = J1(value)
}

data class J1<T>(val value: T)
