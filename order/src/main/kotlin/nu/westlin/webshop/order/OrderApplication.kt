package nu.westlin.webshop.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nu.westlin.webshop.domain.DuplicateOrderIdException
import nu.westlin.webshop.domain.Order
import nu.westlin.webshop.test.orders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.beans
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

@SpringBootApplication
class OrderApplication

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
class OrderRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun routes(repository: OrderRepository) = coRouter {
        // TODO petves: Get orders by customerId
        // TODO petves: Get orders by productId
        "/".nest {
            GET("") {
                ServerResponse.ok().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { order -> ServerResponse.ok().bodyValueAndAwait(order) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val order = request.awaitBody<Order>()
                val result = repository.add(order)
                result.fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add order $order because a order with id ${order.id} already exist", it)
                        ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                    }
                )
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args) {
        addInitializers(
            beans {
                bean {
                    OrderRepository(orders)
                }
            }
        )
    }
}

class OrderRepository(orders: List<Order>) {

    private val orders = orders.toMutableList()

    fun all(): Flow<Order> = this.orders.asFlow()
    fun get(id: Int): Order? = this.orders.firstOrNull { it.id == id }
    fun add(order: Order): Result<Unit> {
        return if (this.orders.none { it.id == order.id }) {
            orders.add(order)
            Result.success(Unit)
        } else {
            Result.failure(DuplicateOrderIdException(order.id))
        }
    }
}
