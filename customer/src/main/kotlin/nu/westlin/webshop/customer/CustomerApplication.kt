package nu.westlin.webshop.customer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.test.customers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
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
@EnableDiscoveryClient
class CustomerApplication

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
class CustomerRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun routes(repository: CustomerRepository) = coRouter {
        "/".nest {
            GET("") {
                ServerResponse.ok().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { customer -> ServerResponse.ok().bodyValueAndAwait(customer) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val customer = request.awaitBody<Customer>()
                val result = repository.add(customer)
                result.fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add customer $customer because a customer with id ${customer.id} already exist", it)
                        ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                    }
                )
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CustomerApplication>(*args) {
        addInitializers(
            beans {
                bean {
                    CustomerRepository(customers)
                }
            }
        )
    }
}

class CustomerRepository(customers: List<Customer>) {

    private val customers = customers.toMutableList()

    fun all(): Flow<Customer> = this.customers.asFlow()
    fun get(id: Int): Customer? = this.customers.firstOrNull { it.id == id }
    fun add(customer: Customer): Result<Unit> {
        return if (this.customers.none { it.id == customer.id }) {
            customers.add(customer)
            Result.success(Unit)
        } else {
            Result.failure(DuplicateCustomerIdException(customer.id))
        }
    }
}
