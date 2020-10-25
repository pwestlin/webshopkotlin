package nu.westlin.webshop.core

import kotlinx.coroutines.flow.Flow
import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.CustomerId
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.domain.DuplicateOrderIdException
import nu.westlin.webshop.domain.DuplicateProductIdException
import nu.westlin.webshop.domain.Order
import nu.westlin.webshop.domain.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
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
@EnableDiscoveryClient
class CoreApplication

@Configuration
class CustomerRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun customerRutes(repository: CustomerRepository) = coRouter {
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

@Configuration
class ProductRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun productRoutes(repository: ProductRepository) = coRouter {
        "/products".nest {
            GET("") {
                ServerResponse.ok().json().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { product -> ServerResponse.ok().bodyValueAndAwait(product) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val product = request.awaitBody<Product>()
                repository.add(product).fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add product $product", it)

                        if (it is DuplicateProductIdException) {
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

@Configuration
class OrderRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun orderRoutes(repository: OrderRepository) = coRouter {
        "/orders".nest {
            GET("") {
                ServerResponse.ok().json().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { order -> ServerResponse.ok().bodyValueAndAwait(order) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val order = request.awaitBody<Order>()
                repository.add(order).fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add order $order", it)

                        if (it is DuplicateOrderIdException) {
                            ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                        } else {
                            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
                        }
                    }
                )
            }
            GET("/customer/{id}") {
                ServerResponse.ok().bodyAndAwait(repository.getByCustomerId(it.pathVariable("id").toInt()))
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CoreApplication>(*args) {
        addInitializers(
            beans {
                bean<HttpCustomerRepository>()
                bean<HttpProductRepository>()
                bean<HttpOrderRepository>()
            }
        )
    }
}

@Configuration
class WebClientConfiguration {

    // petves: You have to have a WebClient.Builder annotated with @LoadBalanced to use Service Discovery
    // and then use that to create a WebClient-bean.
    @Bean
    @LoadBalanced
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    }

    @Bean("customerServiceWebClient")
    fun customerServiceWebClientBuilder(
        @Value("\${customerService.baseUrl}") baseUrl: String,
        webClientBuilder: WebClient.Builder
    ): WebClient = createWebClient(baseUrl, webClientBuilder)

    @Bean("productServiceWebClient")
    fun productServiceWebClient(
        @Value("\${productService.baseUrl}") baseUrl: String,
        webClientBuilder: WebClient.Builder
    ): WebClient = createWebClient(baseUrl, webClientBuilder)

    @Bean("orderServiceWebClient")
    fun orderServiceWebClient(
        @Value("\${orderService.baseUrl}") baseUrl: String,
        webClientBuilder: WebClient.Builder
    ): WebClient = createWebClient(baseUrl, webClientBuilder)

    fun createWebClient(baseUrl: String, webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder()
            .baseUrl(baseUrl)
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

interface ProductRepository {
    suspend fun all(): Flow<Product>
    suspend fun get(id: Int): Product?
    suspend fun add(product: Product): Result<Unit>
}

class HttpProductRepository(
    @Qualifier("productServiceWebClient")
    private val webClient: WebClient
) : ProductRepository {
    override suspend fun all(): Flow<Product> {
        return webClient.get()
            .uri("/")
            .retrieve()
            .bodyToFlow()
    }

    override suspend fun get(id: Int): Product? {
        return webClient.get()
            .uri("/$id")
            .awaitExchange()
            .awaitBodyOrNull()
    }

    override suspend fun add(product: Product): Result<Unit> {
        val statusCode = webClient.post()
            .uri("/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(product)
            .awaitExchange()
            .statusCode()
        return when (statusCode) {
            HttpStatus.OK -> Result.success(Unit)
            HttpStatus.CONFLICT -> Result.failure(DuplicateProductIdException(product.id))
            else -> Result.failure(RuntimeException("Could not add product $product, httpStatus = $statusCode"))
        }
    }
}

interface OrderRepository {
    suspend fun all(): Flow<Order>
    suspend fun get(id: Int): Order?
    suspend fun add(order: Order): Result<Unit>
    suspend fun getByCustomerId(customerId: CustomerId): Flow<Order>
}

class HttpOrderRepository(
    @Qualifier("orderServiceWebClient")
    private val webClient: WebClient
) : OrderRepository {
    override suspend fun all(): Flow<Order> {
        return webClient.get()
            .uri("/")
            .retrieve()
            .bodyToFlow()
    }

    override suspend fun get(id: Int): Order? {
        return webClient.get()
            .uri("/$id")
            .awaitExchange()
            .awaitBodyOrNull()
    }

    override suspend fun add(order: Order): Result<Unit> {
        val statusCode = webClient.post()
            .uri("/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(order)
            .awaitExchange()
            .statusCode()
        return when (statusCode) {
            HttpStatus.OK -> Result.success(Unit)
            HttpStatus.CONFLICT -> Result.failure(DuplicateOrderIdException(order.id))
            else -> Result.failure(RuntimeException("Could not add order $order, httpStatus = $statusCode"))
        }
    }

    override suspend fun getByCustomerId(customerId: CustomerId): Flow<Order> {
        return webClient.get()
            .uri("/customer/$customerId")
            .retrieve()
            .bodyToFlow()
    }
}
