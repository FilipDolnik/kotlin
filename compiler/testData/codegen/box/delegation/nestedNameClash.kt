// DUMP_IR
// IGNORE_BACKEND_K2: ANY
package second

fun box(): String {
    val data = MyClass(object : Base<Base<Int>> {})
    return data.foo()
}

interface Base<A> {
    fun foo(): String = "OK"
}

class MyClass(val prop: second.Base<second.Base<Int>>): Base<Base<Int>> by prop {
    interface Base
}
