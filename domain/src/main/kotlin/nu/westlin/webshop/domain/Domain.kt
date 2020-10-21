package nu.westlin.webshop.domain

import java.time.LocalDateTime

typealias CustomerId = Int
typealias ProductId = Int

data class Customer(val id: Int, val name: String)

data class Product(val id: Int, val name: String, val description: String? = null)
data class OrderRow(val productId: ProductId, val quantity: Int)
data class Order(val id: Int, val customerId: CustomerId, val timestamp: LocalDateTime, val orderRows: List<OrderRow>)