package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.controller.RecommendationDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.createCustomer
import de.codecentric.workshop.hexagonal.cinema.tickets.createFavorites
import de.codecentric.workshop.hexagonal.cinema.tickets.createMovie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.FakeCustomerGenreSuggestionClient
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.FakeMovieRepository
import org.assertj.core.api.Assertions
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
            viewedMovies = emptyList(),
            favorites = createFavorites(movie1.id, movie2.id)
        )

        // When
        val recommendations: List<RecommendationDTO> = sut.calcRecommendations(customer)

        // Then
        assertThat(recommendations).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.5),
            RecommendationDTO(movieId = movie2.id, probability = 0.5),
            RecommendationDTO(movieId = movie3.id, probability = 0.05)
        )
    }

    @Test
    fun `use datakraken API for recommendations if customer has no favorites`() {
        // Given
        val movie1 = movieRepository.save(createMovie(title = "Ace Ventura", genre = Genre.COMEDY))
        val movie2 = movieRepository.save(createMovie(title = "Titanic", genre = Genre.DRAMA))
        val movie3 = movieRepository.save(createMovie(title = "Police Academy", genre = Genre.COMEDY))
        movieRepository.save(createMovie(title = "Die Hard", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Mission Impossible", genre = Genre.ACTION))
        movieRepository.save(createMovie(title = "Mission Impossible 2", genre = Genre.ACTION))

        val customer = createCustomer(
            viewedMovies = emptyList(),
            favorites = emptyList()
        )

        customerGenreSuggestionClient.fixGenres(Genre.COMEDY, Genre.DRAMA)

        // When
        val recommendations = sut.calcRecommendations(customer)

        // Then
        assertThat(recommendations).containsExactlyInAnyOrder(
            RecommendationDTO(movieId = movie1.id, probability = 0.01),
            RecommendationDTO(movieId = movie2.id, probability = 0.01),
            RecommendationDTO(movieId = movie3.id, probability = 0.01)
        )
    }
}