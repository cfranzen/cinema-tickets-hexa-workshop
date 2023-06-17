package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import de.codecentric.workshop.hexagonal.cinema.tickets.createMovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.ACTION
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.COMEDY
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.DRAMA
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.FANTASY
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.ANNOUNCED
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.EXPIRED
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.IN_THEATER
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.PREVIEW
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
internal class MovieSpringRepositoryTest(
    @Autowired private val movieSpringRepository: MovieSpringRepository
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
    fun `movie id is generated automatically`() {
        // Given
        val movie = createMovieEntity()

        // When
        val persistedMovie = movieSpringRepository.save(movie)

        // Then
        assertThat(persistedMovie.id).isGreaterThan(0)
        assertThat(persistedMovie)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(movie)
    }

    @Test
    fun `find movies by set of genres`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(genre = ACTION))
        val movie2 = movieSpringRepository.save(createMovieEntity(genre = COMEDY))
        val movie3 = movieSpringRepository.save(createMovieEntity(genre = DRAMA))
        val movie4 = movieSpringRepository.save(createMovieEntity(genre = ACTION))
        val movie5 = movieSpringRepository.save(createMovieEntity(genre = DRAMA))

        // When / Then
        assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION)))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION, FANTASY)))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION, COMEDY)))
            .containsExactlyInAnyOrder(movie1, movie2, movie4)

        assertThat(movieSpringRepository.findByGenreIn(listOf(DRAMA, COMEDY)))
            .containsExactlyInAnyOrder(movie2, movie3, movie5)

        assertThat(movieSpringRepository.findByGenreIn(listOf(DRAMA, ACTION)))
            .containsExactlyInAnyOrder(movie1, movie3, movie4, movie5)

        assertThat(movieSpringRepository.findByGenreIn(listOf(DRAMA, ACTION, COMEDY, FANTASY)))
            .containsExactlyInAnyOrder(movie1, movie2, movie3, movie4, movie5)
    }

    @Test
    fun `find movies by their state`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(state = IN_THEATER))
        val movie2 = movieSpringRepository.save(createMovieEntity(state = PREVIEW))
        val movie3 = movieSpringRepository.save(createMovieEntity(state = ANNOUNCED))
        val movie4 = movieSpringRepository.save(createMovieEntity(state = IN_THEATER))

        // When / Then
        assertThat(movieSpringRepository.findByState(IN_THEATER))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(movieSpringRepository.findByState(PREVIEW))
            .containsExactlyInAnyOrder(movie2)

        assertThat(movieSpringRepository.findByState(ANNOUNCED))
            .containsExactlyInAnyOrder(movie3)

        assertThat(movieSpringRepository.findByState(EXPIRED)).isEmpty()
    }
}