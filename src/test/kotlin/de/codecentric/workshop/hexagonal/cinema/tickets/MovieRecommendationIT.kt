package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.controller.Recommendation
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieFavorite
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

@SpringBootTest
@AutoConfigureWebTestClient
class MovieRecommendationIT(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val movieRepository: MovieRepository,
    @Autowired private val webTestClient: WebTestClient
) {

    fun `recommend movies to customer from favorites`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.save(createMovie(title = "Titanic", genre = Genre.DRAMA))
        val movie3 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie4 = movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))

        val customer = customerRepository.save(
            createCustomer(
                viewedMovies = emptyList(),
                favoriteMovies = listOf(
                    MovieFavorite(
                        movieId = movie1.id, favoriteSince = Instant.now()
                    ),
                    MovieFavorite(
                        movieId = movie3.id, favoriteSince = Instant.now()
                    )
                )
            )
        )

        // When
        val result = webTestClient
            .get()
            .uri("/recommendation/{customerId}", customer.id)
            .exchange()
            .expectStatus()
            .isOk

        // Then
        result.returnResult(object : ParameterizedTypeReference<List<Recommendation>> {})
    }

}