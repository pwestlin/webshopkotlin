package nu.westlin.webshop.customer

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.test.customers
import nu.westlin.webshop.test.greg
import nu.westlin.webshop.test.jen
import nu.westlin.webshop.test.maria
import nu.westlin.webshop.test.steve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CustomerRepositoryTest {

    private lateinit var repository: CustomerRepository

    @BeforeEach
    fun init() {
        repository = CustomerRepository(customers)
    }

    @Test
    fun `get all customers`() = runBlocking<Unit> {
        assertThat(repository.all().toList()).containsExactlyInAnyOrder(maria, steve, greg)
    }

    @Test
    fun `add a customer`() = runBlocking<Unit> {
        assertThat(repository.add(jen).isSuccess).isTrue

        assertThat(repository.all().toList()).containsExactlyInAnyOrder(maria, steve, greg, jen)
    }

    @Test
    fun `add a customer with a customerId that already exist`() {
        repository.add(greg.copy(name = "George")).exceptionOrNull()!!.let { e ->
            assertThat(e).isInstanceOf(DuplicateCustomerIdException::class.java)
            assertThat(e).hasMessage("A customer with id ${greg.id} already exist")
        }
    }

    @Test
    fun `get a customer by id`() {
        assertThat(repository.get(steve.id)).isEqualTo(steve)
    }

    @Test
    fun `get a customer by an id that does not exist`() {
        assertThat(repository.get(-1)).isNull()
    }
}