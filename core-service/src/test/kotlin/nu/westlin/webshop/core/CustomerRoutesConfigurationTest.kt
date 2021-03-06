package nu.westlin.webshop.core

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.domain.Customer
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.test.customers
import nu.westlin.webshop.test.inlineValue
import nu.westlin.webshop.test.jen
import nu.westlin.webshop.test.maria
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
@Import(CustomerRoutesConfiguration::class)
internal class CustomerRoutesConfigurationTest(@Autowired private val client: WebTestClient) {

    @MockkBean
    private lateinit var repository: CustomerRepository

    @Test
    fun `get all customers`() = runBlocking<Unit> {

        coEvery { repository.all() } returns customers.asFlow()

        val result = client.get()
            .uri("/customers")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Customer>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(customers)
    }

    @Test
    fun `get customer by id`() {
        coEvery { repository.get(maria.id) } returns maria

        client.get()
            .uri("/customers/${maria.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Customer>().isEqualTo(maria)
    }

    @Test
    fun `get customer by id that does not exist`() {
        coEvery { repository.get(jen.id) } returns null

        val result = client.get()
            .uri("/customers/${jen.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a customer with a customer id that does not exist`() {
        coEvery { repository.add(jen) } returns inlineValue(Result.success(Unit))

        val result = client.post()
            .uri("/customers")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(jen) }
    }

    @Test
    fun `add a customer with a customer id that already exist`() {
        coEvery { repository.add(jen) } returns inlineValue(Result.failure(DuplicateCustomerIdException(jen.id)))

        val result = client.post()
            .uri("/customers")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(jen) }
    }

    @Test
    fun `add a customer with a customer id not OK nor CONFLICT`() {
        coEvery { repository.add(jen) } returns inlineValue(Result.failure(RuntimeException("foo")))

        val result = client.post()
            .uri("/customers")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        coVerify { repository.add(jen) }
    }
}