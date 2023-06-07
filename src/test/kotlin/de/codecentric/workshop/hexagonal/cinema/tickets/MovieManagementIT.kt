package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MovieManagementIT(
    @Autowired private val movieRepository: MovieRepository,
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
        movieRepository.deleteAll()
    }

    @Test
    fun `create new movies and assign unique id to each of them`() {
        // Given
        val movie1 = createMovieDTO(title = "Die Hard", genre = Genre.ACTION)
        val movie2 = createMovieDTO(title = "Ace Ventura", genre = Genre.COMEDY)
        val movie3 = createMovieDTO(title = "Mission Impossible", genre = Genre.ACTION)
        val moviesRequest = listOf(movie1, movie2, movie3)

        // When
        val movieResponses = mutableListOf<Movie>()
        moviesRequest.forEach { movie ->
            val result = testRestTemplate.exchange(
                "/movies",
                HttpMethod.POST,
                HttpEntity(movie),
                Movie::class.java
            )
            assertTrue(result.statusCode.is2xxSuccessful)
            result.body?.let { movieResponses.add(it) }
        }

        // Then
        for (i in moviesRequest.indices) {
            assertThat(movieResponses[i])
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(moviesRequest[i].toMovie())
        }

        val movieIds = movieResponses.map { it.id }.toSet()
        assertThat(movieIds).hasSize(moviesRequest.size)
    }
}