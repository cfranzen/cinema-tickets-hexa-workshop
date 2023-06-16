package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.controller.BookingDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.BookingEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Screening
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.ACTION
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.COMEDY
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.ANNOUNCED
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.EXPIRED
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.IN_THEATER
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState.PREVIEW
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
internal class BookingIT(
    @Autowired private val movieSpringRepository: MovieSpringRepository,
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
        movieSpringRepository.deleteAll()
    }

    @Test
    fun `list available screenings for previews`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = ACTION, state = PREVIEW))
        val movie2 = movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = COMEDY, state = PREVIEW))

        // When
        val result = testRestTemplate.exchange(
            "/screenings",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Screening>>() {}
        )

        // Then
        assertThat(result.statusCode.is2xxSuccessful).isTrue()
        assertThat(result.body)
            .containsExactlyInAnyOrder(
                Screening(id = 1, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-14T19:00")),
                Screening(id = 2, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-14T19:00"))
            )
            .usingRecursiveComparison()
    }

    @Test
    fun `list available screenings for movies that are in theater`() {
        // Given
        val movie1 = movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = ACTION, state = IN_THEATER))
        val movie2 = movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = COMEDY, state = IN_THEATER))

        // When
        val result = testRestTemplate.exchange(
            "/screenings",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Screening>>() {}
        )

        // Then
        assertThat(result.statusCode.is2xxSuccessful).isTrue()
        assertThat(result.body)
            .containsExactlyInAnyOrder(
                Screening(id = 1, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-08T14:30")),
                Screening(id = 2, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-08T14:30")),
                Screening(id = 3, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-08T20:00")),
                Screening(id = 4, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-08T20:00")),
                Screening(id = 5, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-09T14:30")),
                Screening(id = 6, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-09T14:30")),
                Screening(id = 7, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-09T20:00")),
                Screening(id = 8, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-09T20:00")),
                Screening(id = 9, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-10T14:30")),
                Screening(id = 10, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-10T14:30")),
                Screening(id = 11, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-10T20:00")),
                Screening(id = 12, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-10T20:00")),
                Screening(id = 13, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-11T14:30")),
                Screening(id = 14, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-11T14:30")),
                Screening(id = 15, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-11T20:00")),
                Screening(id = 16, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-11T20:00")),
                Screening(id = 17, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-12T14:30")),
                Screening(id = 18, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-12T14:30")),
                Screening(id = 19, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-12T20:00")),
                Screening(id = 20, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-12T20:00")),
                Screening(id = 21, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-13T14:30")),
                Screening(id = 22, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-13T14:30")),
                Screening(id = 23, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-13T20:00")),
                Screening(id = 24, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-13T20:00")),
                Screening(id = 25, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-14T14:30")),
                Screening(id = 26, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-14T14:30")),
                Screening(id = 27, movieId = movie1.id, startTime = LocalDateTime.parse("2023-06-14T20:00")),
                Screening(id = 28, movieId = movie2.id, startTime = LocalDateTime.parse("2023-06-14T20:00"))
            )
            .usingRecursiveComparison()
    }

    @Test
    fun `do not create screenings for movies that are announced or expired`() {
        // Given
        movieSpringRepository.save(createMovieEntity(title = "Die Hard", genre = ACTION, state = ANNOUNCED))
        movieSpringRepository.save(createMovieEntity(title = "Ace Ventura", genre = COMEDY, state = EXPIRED))

        // When
        val result = testRestTemplate.exchange(
            "/screenings",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Screening>>() {}
        )

        // Then
        assertThat(result.statusCode.is2xxSuccessful).isTrue()
        assertThat(result.body).isEmpty()
    }

    @Test
    fun `do not create screenings if there are no movies`() {
        // When
        val result = testRestTemplate.exchange(
            "/screenings",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Screening>>() {}
        )

        // Then
        assertThat(result.statusCode.is2xxSuccessful).isTrue()
        assertThat(result.body).isEmpty()
    }

    @Test
    fun `book screening for customer`() {
        // Given
        val customer = customerSpringRepository.save(createCustomerEntity())
        val movie = movieSpringRepository.save(createMovieEntity())

        val request = BookingDTO(
            customerId = customer.id,
            screeningId = 1,
            seats = 3
        )

        // When
        val result = testRestTemplate.exchange(
            "/bookings",
            HttpMethod.POST,
            HttpEntity(request),
            BookingEntity::class.java
        )

        // Then
        assertThat(result.statusCode.is2xxSuccessful).isTrue()
        assertThat(result.body)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(
                BookingEntity(
                    customerId = customer.id,
                    movieId = movie.id,
                    startTime = LocalDateTime.parse("2023-06-08T14:30"),
                    seats = 3,
                    bookedAt = NOW
                )
            )
    }

    @Test
    fun `booking fails if screening ID is invalid`() {
        // Given
        val customer = customerSpringRepository.save(createCustomerEntity())
        movieSpringRepository.save(createMovieEntity())

        val request = BookingDTO(
            customerId = customer.id,
            screeningId = 12345,
            seats = 3
        )

        // When
        val result = testRestTemplate.exchange(
            "/bookings",
            HttpMethod.POST,
            HttpEntity(request),
            ErrorResponse::class.java
        )

        // Then
        assertThat(result.statusCode.isError).isTrue()
    }

    @Test
    fun `booking fails if customer ID is invalid`() {
        // Given
        val customer = customerSpringRepository.save(createCustomerEntity())
        movieSpringRepository.save(createMovieEntity())

        val request = BookingDTO(
            customerId = customer.id + 1,
            screeningId = 1,
            seats = 3
        )

        // When
        val result = testRestTemplate.exchange(
            "/bookings",
            HttpMethod.POST,
            HttpEntity(request),
            ErrorResponse::class.java
        )

        // Then
        assertThat(result.statusCode.isError).isTrue()
    }
}