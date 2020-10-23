package nu.westlin.webshop.product

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.domain.Product
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
class ProductApplication

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
class ProductRoutesConfiguration {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun routes(repository: ProductRepository) = coRouter {
        "/".nest {
            GET("") {
                ServerResponse.ok().bodyAndAwait(repository.all())
            }
            GET("/{id}") {
                repository.get(it.pathVariable("id").toInt())?.let { product -> ServerResponse.ok().bodyValueAndAwait(product) }
                    ?: ServerResponse.notFound().buildAndAwait()
            }
            POST("") { request ->
                val product = request.awaitBody<Product>()
                val result = repository.add(product)
                result.fold(
                    { ServerResponse.ok().buildAndAwait() },
                    {
                        logger.error("Could not add product $product because a product with id ${product.id} already exist", it)
                        ServerResponse.status(HttpStatus.CONFLICT).buildAndAwait()
                    }
                )
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ProductApplication>(*args) {
        addInitializers(
            beans {
                bean {
                    ProductRepository(
                        listOf(
                            Product(1, "Cookie", "Tastes really good"),
                            Product(2, "Chain lube"),
                            Product(3, "Soda"),
                            Product(4, "Engine oil")
                        )
                    )
                }
            }
        )
    }
}

class ProductRepository(products: List<Product>) {

    private val products = products.toMutableList()

    fun all(): Flow<Product> = this.products.asFlow()
    fun get(id: Int): Product? = this.products.firstOrNull { it.id == id }
    fun add(product: Product): Result<Unit> {
        return if (this.products.none { it.id == product.id }) {
            products.add(product)
            Result.success(Unit)
        } else {
            Result.failure(DuplicateCustomerIdException(product.id))
        }
    }
}
