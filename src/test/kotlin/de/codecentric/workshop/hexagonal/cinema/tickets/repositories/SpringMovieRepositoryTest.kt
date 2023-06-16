package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import de.codecentric.workshop.hexagonal.cinema.tickets.createMovie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre.ACTION
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre.COMEDY
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre.DRAMA
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre.FANTASY
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState.ANNOUNCED
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState.IN_THEATER
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState.EXPIRED
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState.PREVIEW
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@DataJdbcTest
@AutoConfigureTestDatabase(replace = NONE)
class SpringMovieRepositoryTest(
    @Autowired private val springMovieRepository: SpringMovieRepository
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

    @Test
    fun `movie id is generated automatically`() {
        // Given
        val movie = createMovie()

        // When
        val persistedMovie = springMovieRepository.save(movie)

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
        val movie1 = springMovieRepository.save(createMovie(genre = ACTION))
        val movie2 = springMovieRepository.save(createMovie(genre = COMEDY))
        val movie3 = springMovieRepository.save(createMovie(genre = DRAMA))
        val movie4 = springMovieRepository.save(createMovie(genre = ACTION))
        val movie5 = springMovieRepository.save(createMovie(genre = DRAMA))

        // When / Then
        assertThat(springMovieRepository.findByGenreIn(listOf(ACTION)))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(springMovieRepository.findByGenreIn(listOf(ACTION, FANTASY)))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(springMovieRepository.findByGenreIn(listOf(ACTION, COMEDY)))
            .containsExactlyInAnyOrder(movie1, movie2, movie4)

        assertThat(springMovieRepository.findByGenreIn(listOf(DRAMA, COMEDY)))
            .containsExactlyInAnyOrder(movie2, movie3, movie5)

        assertThat(springMovieRepository.findByGenreIn(listOf(DRAMA, ACTION)))
            .containsExactlyInAnyOrder(movie1, movie3, movie4, movie5)

        assertThat(springMovieRepository.findByGenreIn(listOf(DRAMA, ACTION, COMEDY, FANTASY)))
            .containsExactlyInAnyOrder(movie1, movie2, movie3, movie4, movie5)
    }

    @Test
    fun `find movies by their state`() {
        // Given
        val movie1 = springMovieRepository.save(createMovie(state = IN_THEATER))
        val movie2 = springMovieRepository.save(createMovie(state = PREVIEW))
        val movie3 = springMovieRepository.save(createMovie(state = ANNOUNCED))
        val movie4 = springMovieRepository.save(createMovie(state = IN_THEATER))

        // When / Then
        assertThat(springMovieRepository.findByState(IN_THEATER))
            .containsExactlyInAnyOrder(movie1, movie4)

        assertThat(springMovieRepository.findByState(PREVIEW))
            .containsExactlyInAnyOrder(movie2)

        assertThat(springMovieRepository.findByState(ANNOUNCED))
            .containsExactlyInAnyOrder(movie3)

        assertThat(springMovieRepository.findByState(EXPIRED)).isEmpty()
    }
}