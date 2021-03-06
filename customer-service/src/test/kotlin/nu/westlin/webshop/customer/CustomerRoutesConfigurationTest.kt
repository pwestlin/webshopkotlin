package nu.westlin.webshop.customer

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
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
    fun `get all customers`() {

        every { repository.all() } returns customers.asFlow()

        val result = client.get()
            .uri("/")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Customer>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(customers)
    }

    @Test
    fun `get customer by id`() {
        every { repository.get(maria.id) } returns maria

        client.get()
            .uri("/${maria.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Customer>().isEqualTo(maria)
    }

    @Test
    fun `get customer by id that does not exist`() {
        every { repository.get(jen.id) } returns null

        val result = client.get()
            .uri("/${jen.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a customer with a customer id that does not exist`() {
        every { repository.add(jen) } returns inlineValue(Result.success(Unit))

        val result = client.post()
            .uri("/")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(jen) }
    }

    @Test
    fun `add a customer with a customer id that already exist`() {
        every { repository.add(jen) } returns inlineValue(Result.failure(DuplicateCustomerIdException(jen.id)))

        val result = client.post()
            .uri("/")
            .bodyValue(jen)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(jen) }
    }
}