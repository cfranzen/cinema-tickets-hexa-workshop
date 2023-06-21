package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.outbound

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.ACTION
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.DRAMA
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createMovieEntity
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@DataJdbcTest
@Import(PostgresRecommendationMovieRepository::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class PostgresRecommendationMovieRepositoryTest(
    @Autowired private val movieSpringRepository: MovieSpringRepository,
    @Autowired private val sut: PostgresRecommendationMovieRepository,
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
    fun `finds movies by genre`() {
        // given
        val expectedActionGenre = ACTION
        val expectedActionMovie = movieSpringRepository.save(createMovieEntity(genre = expectedActionGenre))
        val expectedDramaGenre = DRAMA
        val expectedDramaMovie = movieSpringRepository.save(createMovieEntity(genre = expectedDramaGenre))

        // when
        val result = sut.getMoviesByGenres(listOf(expectedActionGenre, expectedDramaGenre))

        // then
        assertThat(result)
            .containsExactly(
                expectedActionMovie.toDomainObject(),
                expectedDramaMovie.toDomainObject(),
            )
    }

    @Test
    fun `does not find movie when none is given for genre`() {
        // given
        val nonExistentGenre = ACTION
        movieSpringRepository.save(createMovieEntity(genre = DRAMA))

        // when
        val result = sut.getMoviesByGenres(listOf(nonExistentGenre))

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `finds genres for movie IDs`() {
        // given
        val expectedActionGenre = ACTION
        val expectedActionMovie = movieSpringRepository.save(createMovieEntity(genre = expectedActionGenre))
        val expectedDramaGenre = DRAMA
        val expectedDramaMovie = movieSpringRepository.save(createMovieEntity(genre = expectedDramaGenre))

        // when
        val result = sut.getGenresForMovieIds(listOf(expectedActionMovie.id, expectedDramaMovie.id))

        // then
        assertThat(result)
            .containsExactly(
                expectedActionGenre,
                expectedDramaGenre,
            )
    }

    @Test
    fun `returns empty genre list of unknown movie ID`() {
        // given
        movieSpringRepository.save(createMovieEntity(genre = ACTION))
        val unknownId = 12345

        // when
        val result = sut.getGenresForMovieIds(listOf(unknownId))

        // then
        assertThat(result)
            .isEmpty()
    }

    @Test
    fun `finds movies in theater`() {
        // given
        val expectedActionMovie = movieSpringRepository.save(createMovieEntity(state = MovieState.IN_THEATER))
        val expectedDramaMovie = movieSpringRepository.save(createMovieEntity(state = MovieState.IN_THEATER))

        // when
        val result = sut.getMoviesInTheater(listOf(expectedActionMovie.id, expectedDramaMovie.id))

        // then
        assertThat(result)
            .containsExactly(
                expectedActionMovie.toDomainObject(),
                expectedDramaMovie.toDomainObject(),
            )
    }

    @ParameterizedTest
    @EnumSource(MovieState::class, mode = EnumSource.Mode.EXCLUDE, names = ["IN_THEATER"])
    fun `returns empty list if no movies are in theater`(movieState: MovieState) {
        // given
        val movieNotInTheater = movieSpringRepository.save(createMovieEntity(state = movieState))

        // when
        val result = sut.getMoviesInTheater(listOf(movieNotInTheater.id))

        // then
        assertThat(result)
            .isEmpty()
    }


    @Test
    fun `movie id is generated automatically`() {
        // Given
        val movie = createMovieEntity()

        // When
        val persistedMovie = movieSpringRepository.save(movie)

        // Then
        Assertions.assertThat(persistedMovie.id).isGreaterThan(0)
        Assertions.assertThat(persistedMovie)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(movie)
    }

    @Test
    fun `find movies by set of genres`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(genre = ACTION))
        val movie2 = movieSpringRepository.save(createMovieEntity(genre = Genre.COMEDY))
        val movie3 = movieSpringRepository.save(createMovieEntity(genre = DRAMA))
        val movie4 = movieSpringRepository.save(createMovieEntity(genre = ACTION))
        val movie5 = movieSpringRepository.save(createMovieEntity(genre = DRAMA))

        // When / Then
        Assertions.assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION)))
            .containsExactlyInAnyOrder(movie1, movie4)

        Assertions.assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION, Genre.FANTASY)))
            .containsExactlyInAnyOrder(movie1, movie4)

        Assertions.assertThat(movieSpringRepository.findByGenreIn(listOf(ACTION, Genre.COMEDY)))
            .containsExactlyInAnyOrder(movie1, movie2, movie4)

        Assertions.assertThat(movieSpringRepository.findByGenreIn(listOf(DRAMA, Genre.COMEDY)))
            .containsExactlyInAnyOrder(movie2, movie3, movie5)

        Assertions.assertThat(movieSpringRepository.findByGenreIn(listOf(DRAMA, ACTION)))
            .containsExactlyInAnyOrder(movie1, movie3, movie4, movie5)

        Assertions.assertThat(
            movieSpringRepository.findByGenreIn(
                listOf(
                    DRAMA,
                    ACTION,
                    Genre.COMEDY,
                    Genre.FANTASY
                )
            )
        )
            .containsExactlyInAnyOrder(movie1, movie2, movie3, movie4, movie5)
    }

    @Test
    fun `find movies by their state`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(state = MovieState.IN_THEATER))
        val movie2 = movieSpringRepository.save(createMovieEntity(state = MovieState.PREVIEW))
        val movie3 = movieSpringRepository.save(createMovieEntity(state = MovieState.ANNOUNCED))
        val movie4 = movieSpringRepository.save(createMovieEntity(state = MovieState.IN_THEATER))

        // When / Then
        Assertions.assertThat(movieSpringRepository.findByState(MovieState.IN_THEATER))
            .containsExactlyInAnyOrder(movie1, movie4)

        Assertions.assertThat(movieSpringRepository.findByState(MovieState.PREVIEW))
            .containsExactlyInAnyOrder(movie2)

        Assertions.assertThat(movieSpringRepository.findByState(MovieState.ANNOUNCED))
            .containsExactlyInAnyOrder(movie3)

        Assertions.assertThat(movieSpringRepository.findByState(MovieState.EXPIRED)).isEmpty()
    }

    private fun MovieEntity.toDomainObject() = Movie(
        id = this.id,
        title = this.title,
        genre = this.genre,
        state = this.state
    )

}