package nu.westlin.webshop.core

import kotlinx.coroutines.flow.Flow
import nu.westlin.webshop.domain.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.beans
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.bodyToFlow
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json


@SpringBootApplication
class CoreApplication

fun main(args: Array<String>) {
    runApplication<CoreApplication>(*args) {
        val logger: Logger = LoggerFactory.getLogger(this.javaClass)

        addInitializers(
            beans {
                bean<HttpCustomerRepository>()

                // TODO petves: Refact out and test
                bean {
                    coRouter {
                        val repository = ref<CustomerRepository>()
                        GET("/customers") {
                            ServerResponse.ok().json().bodyAndAwait(repository.all())
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
                            } catch (e: RuntimeException) {
                                logger.error("Could not add customer $customer", e)
                                ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
                            }
                        }

                    }
                }
            }
        )
    }
}

@Configuration
class WebClientConfiguration {

    @Bean("customerServiceWebClient")
    fun customerServiceWebClient(
        @Value("\${customerService.baseUrl}") baseUrl: String
    ): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}

interface CustomerRepository {
    suspend fun all(): Flow<Customer>
    suspend fun get(id: Int): Customer?
    suspend fun add(customer: Customer)
}

// TODO petves: Test
class HttpCustomerRepository(
    @Qualifier("customerServiceWebClient")
    private val webClient: WebClient
) : CustomerRepository {
    override suspend fun all(): Flow<Customer> {
        return webClient.get()
            .uri("/customers")
            .awaitExchange()
            .bodyToFlow()
    }

    override suspend fun get(id: Int): Customer? {
        return webClient.get()
            .uri("/customers/$id")
            .awaitExchange()
            .awaitBodyOrNull()
    }

    override suspend fun add(customer: Customer) {
        // TODO petves: Check result?
        webClient.post()
            .uri("/customers")
            .bodyValue(customer)
            .awaitExchange()
    }
}

