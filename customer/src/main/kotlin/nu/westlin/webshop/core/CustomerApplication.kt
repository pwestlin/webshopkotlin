package nu.westlin.webshop.core

import nu.westlin.webshop.domain.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

@SpringBootApplication
class CustomerApplication

fun main(args: Array<String>) {
    runApplication<CustomerApplication>(*args) {
        val logger: Logger = LoggerFactory.getLogger(this.javaClass)

        addInitializers(
            beans {
                bean<CustomerRepository>()

                // TODO petves: Refact out and test
                bean {
                    coRouter {
                        val repository = ref<CustomerRepository>()
                        GET("/customers") {
                            ServerResponse.ok().bodyValueAndAwait(repository.all())
                        }
                        GET("/customers/{id}") {
                            repository.get(it.pathVariable("id").toInt())?.let { customer -> ServerResponse.ok().bodyValueAndAwait(customer) }
                                ?: ServerResponse.notFound().buildAndAwait()
                        }
                        POST("/customers") { request ->
                            val customer = request.awaitBody<Customer>()
                            try {
                                repository.add(customer)
                                ServerResponse.ok().buildAndAwait()
                            } catch (e: DuplicateCustomerIdException) {
                                logger.error("Could not add customer $customer because a customer with id ${customer.id} already exist", e)
                                ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                            }
                        }
                    }
                }
            }
        )
    }
}

// TODO petves: Test
class CustomerRepository {

    private val list = mutableListOf(
        Customer(1, "Camilla"),
        Customer(2, "Peter"),
        Customer(3, "Adam"),
        Customer(4, "Felix")
    )

    fun all(): List<Customer> = list.toList()
    fun get(id: Int): Customer? = list.firstOrNull { it.id == id }
    fun add(customer: Customer) {
        // TODO petves: Use kotlin.Result instead of exception?
        if(list.none { it.id == customer.id }) {
            list.add(customer)
        } else {
            throw DuplicateCustomerIdException("A customer with id ${customer.id} already exist")
        }

    }
}

class DuplicateCustomerIdException(message: String) : RuntimeException(message)