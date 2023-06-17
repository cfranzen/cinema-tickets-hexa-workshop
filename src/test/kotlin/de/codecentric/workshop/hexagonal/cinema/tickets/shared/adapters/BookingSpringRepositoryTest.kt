package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createBookingEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@DataJdbcTest
@AutoConfigureTestDatabase(replace = NONE)
internal class BookingSpringRepositoryTest(
    @Autowired private val bookingSpringRepository: BookingSpringRepository
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

    @Test
    fun `booking id is generated automatically`() {
        // Given
        val booking = createBookingEntity()

        // When
        val persistedBooking = bookingSpringRepository.save(booking)

        // Then
        assertThat(persistedBooking.id).isGreaterThan(0)
        assertThat(persistedBooking)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(booking)
    }


}