package nu.westlin.webshop.test

import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.Order
import nu.westlin.webshop.domain.OrderRow
import nu.westlin.webshop.domain.Product
import java.time.LocalDateTime

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

val order1 = Order(
    1,
    maria.id,
    LocalDateTime.now().minusDays(3),
    listOf(
        OrderRow(cookie.id, 42),
        OrderRow(soda.id, 5)
    )
)
val order2 = Order(
    2,
    maria.id,
    LocalDateTime.now().minusHours(5),
    listOf(
        OrderRow(chainLube.id, 3),
        OrderRow(engineOil.id, 1)
    )
)
val order3 = Order(
    3,
    steve.id,
    LocalDateTime.now().minusDays(421).minusHours(5),
    listOf(
        OrderRow(chainLube.id, 3),
        OrderRow(engineOil.id, 1)
    )
)
val order4 = Order(
    4,
    steve.id,
    LocalDateTime.now().minusDays(4).minusHours(1),
    listOf(
        OrderRow(chainLube.id, 1),
        OrderRow(engineOil.id, 3)
    )
)
val orders = listOf(
    order1,
    order2,
    order3
)