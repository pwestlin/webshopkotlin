package nu.westlin.webshop.test

import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.Product

val maria = Customer(1, "Maria")
val steve = Customer(2, "Steve")
val greg = Customer(3, "Greg")
val jen = Customer(4, "Jen")

val customers = listOf(
    maria,
    steve,
    greg
)

val cookie = Product(1, "Cookie", "Tastes really good")
val chainLube = Product(2, "Chain lube")
val soda = Product(3, "Soda")
val engineOil = Product(4, "Engine oil")

val products = listOf(
    cookie,
    chainLube,
    soda
)