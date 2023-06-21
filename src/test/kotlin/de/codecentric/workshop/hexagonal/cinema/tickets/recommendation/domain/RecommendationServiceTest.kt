package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

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
        val movie1 = movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        val movie2 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie3 = movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Titanic", genre = Genre.DRAMA))

        val customer = createCustomer(
            favorites = listOf(movie1.id, movie2.id)
        )

        // When
        val recommendations = sut.calcRecommendations(customer)

        // Then
        assertThat(recommendations).containsExactlyInAnyOrder(
            Recommendation(customer = customer, movie = movie1, probability = 0.5),
            Recommendation(customer = customer, movie = movie2, probability = 0.5),
            Recommendation(customer = customer, movie = movie3, probability = 0.05)
        )
    }

    @Test
    fun `use genre suggestion service API for recommendations if customer has no favorites`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie2 = movieRepository.save(createMovie(title = "Titanic", genre = Genre.DRAMA))
        val movie3 = movieRepository.save(createMovie(title = "Police Academy", genre = Genre.COMEDY))
        movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Mission Impossible 2", genre = Genre.ACTION))

        val customer = createCustomer(
            favorites = emptyList()
        )
        customerGenreSuggestionClient.addMapping(customer, Genre.COMEDY, Genre.DRAMA)

        // When
        val recommendations = sut.calcRecommendations(customer)

        // Then
        assertThat(recommendations).containsExactlyInAnyOrder(
            Recommendation(customer = customer, movie = movie1, probability = 0.01),
            Recommendation(customer = customer, movie = movie2, probability = 0.01),
            Recommendation(customer = customer, movie = movie3, probability = 0.01)
        )
    }
}