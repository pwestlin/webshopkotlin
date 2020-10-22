package nu.westlin.webshop.core

import nu.westlin.webshop.domain.Customer

// TODO petves: Move this to separate module and import where it's needed?


val maria = Customer(1, "Maria")
val steve = Customer(2, "Steve")
val greg = Customer(3, "Greg")
val jen = Customer(4, "Jen")

val customers = listOf(
    maria,
    steve,
    greg
)
