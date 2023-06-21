package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.inbound.RecommendationDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.FakeCustomerGenreSuggestionClient
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.FakeMovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createCustomer
import de.codecentric.workshop.hexagonal.cinema.tickets.tests.createMovie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RecommendationServiceTest {

    private val movieRepository = FakeMovieRepository()

    private val customerGenreSuggestionClient = FakeCustomerGenreSuggestionClient()

    private val sut = RecommendationService(
        movieRepository = movieRepository,
        customerGenreSuggestionClient = customerGenreSuggestionClient
    )

    @Test
    fun `recommend movies to customer from favorites, filling up to 3 by equal genre`() {
        // Given
        val movie1 = movieRepository.add(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.add(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieRepository.add(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        movieRepository.add(createMovie(title = "Titanic", genre = Genre.DRAMA))

        val customer = createCustomer(
            favorites = listOf(movie1.id, movie2.id)
        )

        // When
        val recommendations = sut.calcRecommendations(customer)

        // Then
        assertThat(recommendations).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.5),
            RecommendationDTO(movieId = movie2.id, probability = 0.5),
            RecommendationDTO(movieId = movie3.id, probability = 0.05)
        )
    }
}