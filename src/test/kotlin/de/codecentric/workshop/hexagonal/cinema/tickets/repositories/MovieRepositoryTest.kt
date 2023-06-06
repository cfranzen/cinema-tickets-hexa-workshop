package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import de.codecentric.workshop.hexagonal.cinema.tickets.createMovie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
class MovieRepositoryTest(
    @Autowired private val movieRepository: MovieRepository
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
        movieRepository.deleteAll()
    }

    @Test
    fun `movie id is generated automatically`() {
        // Given
        val movie = createMovie()

        // When
        val persistedMovie = movieRepository.save(movie)

        // Then
        assertThat(persistedMovie.id).isGreaterThan(0)
        assertThat(persistedMovie)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(movie)
    }


}