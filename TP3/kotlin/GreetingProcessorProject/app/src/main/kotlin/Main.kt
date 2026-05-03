import app.MyClass
import app.MyClassWrapper


fun main () {
    val myClass = MyClass()
    val wrappedMyClass = MyClassWrapper(myClass) // Use the wrapper class
    wrappedMyClass.sayHello ()
    wrappedMyClass.compute ()
}