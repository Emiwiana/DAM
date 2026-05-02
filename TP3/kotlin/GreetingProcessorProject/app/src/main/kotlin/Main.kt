package com.example.app

fun main () {
    val myClassWrapper = MyClassWrapper ()
    val wrappedMyClass = MyClassWrapper ( myClassWrapper ) // Use the wrapper class
    wrappedMyClass . sayHello ()
    wrappedMyClass . compute ()
}