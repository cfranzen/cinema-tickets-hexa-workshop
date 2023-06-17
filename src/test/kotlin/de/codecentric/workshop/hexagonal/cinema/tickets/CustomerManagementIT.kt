package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustomerManagementIT(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val testRestTemplate: TestRestTemplate
) {

    companion object {
        @Container
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .withReuse(true)
            .waitingFor(Wait.forListeningPort())

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @AfterEach
    fun cleanupData() {
        customerRepository.deleteAll()
    }

    @Test
    fun `create new customers and assign unique id to each of them`() {
        // Given
        val customer1 = createCustomerDTO(name = "Peter Brown", favorites = listOf(1, 2))
        val customer2 = createCustomerDTO(name = "Claudia White", favorites = listOf(4))
        val customer3 = createCustomerDTO(name = "Chi Li", favorites = emptyList())
        val customersRequest = listOf(customer1, customer2, customer3)

        // When
        val customersResponses = mutableListOf<Customer>()
        customersRequest.forEach { customer ->
            val result = testRestTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                HttpEntity(customer),
                Customer::class.java
            )
            assertTrue(result.statusCode.is2xxSuccessful)
            result.body?.let { customersResponses.add(it) }
        }

        // Then
        for (i in customersRequest.indices) {
            Assertions.assertThat(customersResponses[i])
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customersRequest[i].toCustomer(NOW))
        }

        val customerIds = customersResponses.map { it.id }.toSet()
        Assertions.assertThat(customerIds).hasSize(customersRequest.size)
    }

    @Test
    fun `retrieve existing customer by its ID`() {
        // Given
        val customer1 = customerRepository.save(createCustomer(name = "Peter Brown", favorites = createFavorites(1, 2)))
        val customer2 = customerRepository.save(createCustomer(name = "Claudia White", favorites = createFavorites(4)))
        val customer3 = customerRepository.save(createCustomer(name = "Chi Li", favorites = createFavorites()))
        val customersRequest = listOf(customer1, customer2, customer3)

        // When
        val customerResponses = mutableListOf<Customer>()
        customersRequest.forEach { customer ->
            val result = testRestTemplate.exchange(
                "/customers/{id}",
                HttpMethod.GET,
                null,
                Customer::class.java,
                mapOf("id" to customer.id)
            )
            assertTrue(result.statusCode.is2xxSuccessful)
            result.body?.let { customerResponses.add(it) }
        }

        // Then
        for (i in customersRequest.indices) {
            Assertions.assertThat(customerResponses[i])
                .usingRecursiveComparison()
                .isEqualTo(customersRequest[i])
        }
    }

    @Test
    fun `throw HTTP 404 NOT FOUND if movie does not exists`() {
        // Given
        val customer = createCustomer(name = "Chi Li", favorites = createFavorites())
        val invalidMovieId = customer.id + 1

        // When
        val result = testRestTemplate.exchange(
            "/customers/{id}",
            HttpMethod.GET,
            null,
            Void::class.java,
            mapOf("id" to invalidMovieId)
        )

        // Then
        assertTrue(result.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND))
    }
}