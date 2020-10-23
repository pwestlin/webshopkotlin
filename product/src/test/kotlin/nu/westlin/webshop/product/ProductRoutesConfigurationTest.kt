package nu.westlin.webshop.product

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import nu.westlin.webshop.domain.DuplicateProductIdException
import nu.westlin.webshop.domain.Product
import nu.westlin.webshop.test.inlineValue
import nu.westlin.webshop.test.products
import nu.westlin.webshop.test.soda
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
    fun `get all products`() {

        every { repository.all() } returns products.asFlow()

        val result = client.get()
            .uri("/")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Product>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(products)
    }

    @Test
    fun `get product by id`() {
        val product = soda
        every { repository.get(product.id) } returns product

        client.get()
            .uri("/${product.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Product>().isEqualTo(product)
    }

    @Test
    fun `get product by id that does not exist`() {
        val product = soda
        every { repository.get(product.id) } returns null

        val result = client.get()
            .uri("/${product.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a product with a product id that does not exist`() {
        val product = soda
        every { repository.add(product) } returns inlineValue(Result.success(Unit))

        val result = client.post()
            .uri("/")
            .bodyValue(product)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(product) }
    }

    @Test
    fun `add a product with a product id that already exist`() {
        val product = soda
        every { repository.add(product) } returns inlineValue(Result.failure(DuplicateProductIdException(product.id)))

        val result = client.post()
            .uri("/")
            .bodyValue(product)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(product) }
    }
}