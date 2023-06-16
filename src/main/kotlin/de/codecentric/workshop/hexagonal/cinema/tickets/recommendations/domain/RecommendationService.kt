package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.inbound.RecommendationDTO
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound.CustomerGenreSuggestionRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.ports.outbound.MovieRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val customerRepository: CustomerRepository,
    private val suggestionRepository: CustomerGenreSuggestionRepository,
    private val movieRepository: MovieRepository
) {
    companion object {
        private const val MIN_RECOMMENDATIONS = 3
    }

    fun calcRecommendations(customer: Customer): List<RecommendationDTO> {
        val recommendations = mutableListOf<RecommendationDTO>()
        recommendations.addAll(recommendByFavorites(customer))

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            recommendations.addAll(fillUpByEqualGenre(recommendations))
        }

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            recommendations.addAll(fillUpByDatakrakenRecommendations(customer, recommendations))
        }

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            throw IllegalStateException("Could not recommend movies for customer with ID ${customer.id}")
        }
        return recommendations
    }

    private fun recommendByFavorites(customer: Customer): List<RecommendationDTO> {
        val favoriteMovieIds = customer.data.favorites.map { it.movieId }
        return movieRepository
            .getMoviesInTheater(favoriteMovieIds)
            .map { RecommendationDTO(it.id, 0.5) }
    }

    private fun fillUpByEqualGenre(currentRecommendations: List<RecommendationDTO>): List<RecommendationDTO> {
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieIds = currentRecommendations.map { it.movieId }
        val currentGenres = movieRepository.getGenresForMovieIds(movieIds)
        val movieFavoriteCount = calculateMoviesFavoriteCount()
        return movieRepository
            .getMoviesByGenres(currentGenres)
            .filter { !movieIds.contains(it.id) }
            .sortedBy { movieFavoriteCount.getOrDefault(it.id, 0) }
            .reversed()
            .take(missingRecommendations)
            .map { RecommendationDTO(it.id, 0.05) }
    }

    private fun fillUpByDatakrakenRecommendations(
        customer: Customer,
        currentRecommendations: List<RecommendationDTO>
    ): List<RecommendationDTO> {
        val suggestedGenres = suggestionRepository.suggestGenres(customer)
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieFavoriteCount = calculateMoviesFavoriteCount()
        return movieRepository
            .getMoviesByGenres(suggestedGenres)
            .sortedBy { movieFavoriteCount.getOrDefault(it.id, 0) }
            .reversed()
            .take(missingRecommendations)
            .map { RecommendationDTO(it.id, 0.01) }
    }

    private fun calculateMoviesFavoriteCount() = customerRepository
        .findAll()
        .flatMap { customer -> customer.data.favorites.map { it.movieId } }
        .groupingBy { it }
        .eachCount()
}