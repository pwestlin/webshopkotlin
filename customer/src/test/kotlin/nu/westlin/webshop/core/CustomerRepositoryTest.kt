package nu.westlin.webshop.core

import nu.westlin.webshop.domain.Customer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CustomerRepositoryTest {

    private val maria = Customer(1, "Maria")
    private val steve = Customer(2, "Steve")
    private val greg = Customer(3, "Greg")
    private val jen = Customer(4, "Jen")

    private val customers = listOf(
        maria,
        steve,
        greg
    )

    private lateinit var repository: CustomerRepository

    @BeforeEach
    fun init() {
        repository = CustomerRepository(customers)
    }

    @Test
    fun `get all customers`() {
        assertThat(repository.all()).containsExactlyInAnyOrder(maria, steve, greg)
    }

    @Test
    fun `add a customer`() {
        repository.add(jen)

        assertThat(repository.all()).containsExactlyInAnyOrder(maria, steve, greg, jen)
    }

    @Test
    fun `add a customer with a customerId that already exist`() {
        assertThatThrownBy { repository.add(greg.copy(name = "George")) }
            .isInstanceOf(DuplicateCustomerIdException::class.java)
            .hasMessage("A customer with id ${greg.id} already exist")
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