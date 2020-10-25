package nu.westlin.webshop.product

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.domain.DuplicateCustomerIdException
import nu.westlin.webshop.test.chainLube
import nu.westlin.webshop.test.cookie
import nu.westlin.webshop.test.engineOil
import nu.westlin.webshop.test.products
import nu.westlin.webshop.test.soda
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ProductRepositoryTest {

    private lateinit var repository: ProductRepository

    @BeforeEach
    fun init() {
        repository = ProductRepository(products)
    }

    @Test
    fun `get all customers`() = runBlocking<Unit> {
        assertThat(repository.all().toList()).containsExactlyInAnyOrder(cookie, chainLube, soda)
    }

    @Test
    fun `add a customer`() = runBlocking<Unit> {
        assertThat(repository.add(engineOil).isSuccess).isTrue

        assertThat(repository.all().toList()).containsExactlyInAnyOrder(cookie, chainLube, soda, engineOil)
    }

    @Test
    fun `add a customer with a customerId that already exist`() {
        repository.add(chainLube.copy(name = "George")).exceptionOrNull()!!.let { e ->
            assertThat(e).isInstanceOf(DuplicateCustomerIdException::class.java)
            assertThat(e).hasMessage("A customer with id ${chainLube.id} already exist")
        }
    }

    @Test
    fun `get a customer by id`() {
        assertThat(repository.get(chainLube.id)).isEqualTo(chainLube)
    }

    @Test
    fun `get a customer by an id that does not exist`() {
        assertThat(repository.get(-1)).isNull()
    }
}