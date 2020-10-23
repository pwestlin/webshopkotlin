package nu.westlin.webshop.core

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.domain.DuplicateProductIdException
import nu.westlin.webshop.domain.Product
import nu.westlin.webshop.test.chainLube
import nu.westlin.webshop.test.cookie
import nu.westlin.webshop.test.inlineValue
import nu.westlin.webshop.test.products
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList

@WebFluxTest
@Import(ProductRoutesConfiguration::class)
internal class ProductRoutesConfigurationTest(@Autowired private val client: WebTestClient) {

    @MockkBean
    private lateinit var repository: ProductRepository

    @Test
    fun `get all products`() = runBlocking<Unit> {

        coEvery { repository.all() } returns products.asFlow()

        val result = client.get()
            .uri("/products")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Product>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(products)
    }

    @Test
    fun `get product by id`() {
        val product = cookie
        coEvery { repository.get(product.id) } returns product

        client.get()
            .uri("/products/${product.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Product>().isEqualTo(product)
    }

    @Test
    fun `get product by id that does not exist`() {
        val product = chainLube
        coEvery { repository.get(product.id) } returns null

        val result = client.get()
            .uri("/products/${product.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a product with a product id that does not exist`() {
        val product = cookie
        coEvery { repository.add(product) } returns inlineValue(Result.success(Unit))

        val result = client.post()
            .uri("/products")
            .bodyValue(product)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(product) }
    }

    @Test
    fun `add a product with a product id that already exist`() {
        val product = cookie
        coEvery { repository.add(product) } returns inlineValue(Result.failure(DuplicateProductIdException(product.id)))

        val result = client.post()
            .uri("/products")
            .bodyValue(product)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(product) }
    }

    @Test
    fun `add a product with a product id not OK nor CONFLICT`() {
        val product = cookie
        coEvery { repository.add(product) } returns inlineValue(Result.failure(RuntimeException("foo")))

        val result = client.post()
            .uri("/products")
            .bodyValue(product)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(product) }
    }
}