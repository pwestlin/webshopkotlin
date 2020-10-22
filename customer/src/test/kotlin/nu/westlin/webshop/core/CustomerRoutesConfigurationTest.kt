package nu.westlin.webshop.core

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import nu.westlin.webshop.domain.Customer
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
    fun `get all customers`() {

        every { repository.all() } returns customers

        val result = client.get()
            .uri("/customers")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Customer>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(customers)
    }

    @Test
    fun `get customer by id`() {
        every { repository.get(maria.id) } returns maria

        client.get()
            .uri("/customers/${maria.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Customer>().isEqualTo(maria)
    }

    @Test
    fun `get customer by id that does not exist`() {
        every { repository.get(jen.id) } returns null

        val result = client.get()
            .uri("/customers/${jen.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a customer with a customer id that does not exist`() {
        every { repository.add(jen) } returns Unit

        val result = client.post()
            .uri("/customers")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(jen) }
    }

    @Test
    fun `add a customer with a customer id that already exist`() {
        every { repository.add(jen) } throws DuplicateCustomerIdException("Foo bar")

        val result = client.post()
            .uri("/customers")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(jen) }
    }
}