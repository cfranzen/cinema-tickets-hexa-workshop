package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
internal class CustomerManagementIT(
    @Autowired private val customerSpringRepository: CustomerSpringRepository,
    @Autowired private val testRestTemplate: TestRestTemplate
) {

    companion object {
        @Container
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .withReuse(true)

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
        customerSpringRepository.deleteAll()
    }

    @Test
    fun `create new customers and assign unique id to each of them`() {
        // Given
        val customer1 = createCustomerDTO(name = "Peter Brown", favorites = listOf(1, 2))
        val customer2 = createCustomerDTO(name = "Claudia White", favorites = listOf(4))
        val customer3 = createCustomerDTO(name = "Chi Li", favorites = emptyList())
        val customersRequest = listOf(customer1, customer2, customer3)

        // When
        val customersResponses = mutableListOf<CustomerEntity>()
        customersRequest.forEach { customer ->
            val result = testRestTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                HttpEntity(customer),
                CustomerEntity::class.java
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
        val customer1 = customerSpringRepository.save(createCustomerEntity(name = "Peter Brown", favorites = createFavoritesEntity(1, 2)))
        val customer2 = customerSpringRepository.save(createCustomerEntity(name = "Claudia White", favorites = createFavoritesEntity(4)))
        val customer3 = customerSpringRepository.save(createCustomerEntity(name = "Chi Li", favorites = createFavoritesEntity()))
        val customersRequest = listOf(customer1, customer2, customer3)

        // When
        val customerResponses = mutableListOf<CustomerEntity>()
        customersRequest.forEach { customer ->
            val result = testRestTemplate.exchange(
                "/customers/{id}",
                HttpMethod.GET,
                null,
                CustomerEntity::class.java,
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
        val customer = createCustomerEntity(name = "Chi Li", favorites = createFavoritesEntity())
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