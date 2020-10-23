package nu.westlin.webshop.order

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import nu.westlin.webshop.domain.DuplicateOrderIdException
import nu.westlin.webshop.domain.Order
import nu.westlin.webshop.test.inlineValue
import nu.westlin.webshop.test.order1
import nu.westlin.webshop.test.order3
import nu.westlin.webshop.test.orders
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
@Import(OrderRoutesConfiguration::class)
internal class OrderRoutesConfigurationTest(@Autowired private val client: WebTestClient) {

    @MockkBean
    private lateinit var repository: OrderRepository

    @Test
    fun `get all orders`() {

        every { repository.all() } returns orders.asFlow()

        val result = client.get()
            .uri("/")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().returnResult()
        assertThat(result.responseBody).containsExactlyElementsOf(orders)
    }

    @Test
    fun `get order by id`() {
        val order = order1
        every { repository.get(order.id) } returns order

        client.get()
            .uri("/${order.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Order>().isEqualTo(order)
    }

    @Test
    fun `get order by id that does not exist`() {
        val order = order1
        every { repository.get(order.id) } returns null

        val result = client.get()
            .uri("/${order.id}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()
    }

    @Test
    fun `add a order with a order id that does not exist`() {
        val order = order1
        every { repository.add(order) } returns inlineValue(Result.success(Unit))

        val result = client.post()
            .uri("/")
            .bodyValue(order)
            .exchange()
            .expectStatus().isOk
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(order) }
    }

    @Test
    fun `add a order with a order id that already exist`() {
        val order = order3
        every { repository.add(order) } returns inlineValue(Result.failure(DuplicateOrderIdException(order.id)))

        val result = client.post()
            .uri("/")
            .bodyValue(order)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody<Any>().returnResult()
        assertThat(result.responseBody).isNull()

        verify { repository.add(order) }
    }
}