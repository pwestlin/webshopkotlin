package nu.westlin.webshop.core

import kotlinx.coroutines.flow.Flow
import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.beans
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

@Configuration
class CustomerRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun routes(repository: CustomerRepository) = coRouter {
        "/customers".nest {
            GET("") {
                ServerResponse.ok().json().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { customer -> ServerResponse.ok().bodyValueAndAwait(customer) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val customer = request.awaitBody<Customer>()
                repository.add(customer).fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add customer $customer", it)

                        if (it is DuplicateCustomerIdException) {
                            ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                        } else {
                            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
                        }
                    }
                )
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CoreApplication>(*args) {
        addInitializers(
            beans {
                bean<HttpCustomerRepository>()
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
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}

interface CustomerRepository {
    suspend fun all(): Flow<Customer>
    suspend fun get(id: Int): Customer?
    suspend fun add(customer: Customer): Result<Unit>
}

class HttpCustomerRepository(
    @Qualifier("customerServiceWebClient")
    private val webClient: WebClient
) : CustomerRepository {
    override suspend fun all(): Flow<Customer> {
        return webClient.get()
            .uri("/")
            .retrieve()
            .bodyToFlow()
    }

    override suspend fun get(id: Int): Customer? {
        return webClient.get()
            .uri("/$id")
            .awaitExchange()
            .awaitBodyOrNull()
    }

    override suspend fun add(customer: Customer): Result<Unit> {
        val statusCode = webClient.post()
            .uri("/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(customer)
            .awaitExchange()
            .statusCode()
        return when (statusCode) {
            HttpStatus.OK -> Result.success(Unit)
            HttpStatus.CONFLICT -> Result.failure(DuplicateCustomerIdException(customer.id))
            else -> Result.failure(RuntimeException("Could not add customer $customer, httpStatus = $statusCode"))
        }
    }
}
