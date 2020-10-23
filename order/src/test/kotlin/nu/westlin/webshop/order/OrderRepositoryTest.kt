package nu.westlin.webshop.order

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.domain.DuplicateOrderIdException
import nu.westlin.webshop.test.order1
import nu.westlin.webshop.test.order2
import nu.westlin.webshop.test.order3
import nu.westlin.webshop.test.order4
import nu.westlin.webshop.test.orders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OrderRepositoryTest {

    private lateinit var repository: OrderRepository

    @BeforeEach
    fun init() {
        repository = OrderRepository(orders)
    }

    @Test
    fun `get all orders`() = runBlocking<Unit> {
        assertThat(repository.all().toList()).containsExactlyInAnyOrder(order1, order2, order3)
    }

    @Test
    fun `add a order`() = runBlocking<Unit> {
        assertThat(repository.add(order4).isSuccess).isTrue

        assertThat(repository.all().toList()).containsExactlyInAnyOrder(order1, order2, order3, order4)
    }

    @Test
    fun `add a order with an orderId that already exist`() {
        val order = order1
        repository.add(order.copy()).exceptionOrNull()!!.let { e ->
            assertThat(e).isInstanceOf(DuplicateOrderIdException::class.java)
            assertThat(e).hasMessage("An order with id ${order.id} already exist")
        }
    }

    @Test
    fun `get a order by id`() {
        val order = order1
        assertThat(repository.get(order.id)).isEqualTo(order)
    }

    @Test
    fun `get a order by an id that does not exist`() {
        assertThat(repository.get(-1)).isNull()
    }
}