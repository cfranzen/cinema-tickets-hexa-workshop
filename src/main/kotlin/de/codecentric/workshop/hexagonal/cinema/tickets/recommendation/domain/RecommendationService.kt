package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.CustomerGenreSuggestionClient
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.ports.MovieRepository
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val movieRepository: MovieRepository,
    private val customerGenreSuggestionClient: CustomerGenreSuggestionClient
) {
    companion object {
        private const val MIN_RECOMMENDATIONS = 3
    }

    fun calcRecommendations(customer: Customer): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        recommendations.addAll(recommendByFavorites(customer))

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            recommendations.addAll(fillUpByEqualGenre(customer, recommendations))
        }

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            recommendations.addAll(fillUpByDatakrakenRecommendations(customer, recommendations))
        }

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            throw IllegalStateException("Could not recommend movies for customer with ID ${customer.id}")
        }
        return recommendations
    }


    private fun recommendByFavorites(customer: Customer): List<Recommendation> {
        return movieRepository
            .getMoviesInTheater(customer.favoriteMovieIds)
            .map { Recommendation(customer, it, 0.5) }
    }

    private fun fillUpByEqualGenre(
        customer: Customer,
        currentRecommendations: List<Recommendation>
    ): List<Recommendation> {
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieIds = currentRecommendations.map { it.movie.id }
        val currentGenres = movieRepository.getGenresForMovieIds(movieIds)
        return movieRepository
            .getMoviesByGenres(currentGenres)
            .filter { !movieIds.contains(it.id) }
            .sortedBy { it.id }
            .take(missingRecommendations)
            .map { Recommendation(customer, it, 0.05) }
    }

    private fun fillUpByDatakrakenRecommendations(
        customer: Customer,
        currentRecommendations: List<Recommendation>
    ): List<Recommendation> {
        val suggestedGenres = customerGenreSuggestionClient.suggestGenres(customer)
        if (suggestedGenres.isEmpty()) {
            return emptyList()
        }

        val movieIds = currentRecommendations.map { it.movie.id }.toSet()
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        return movieRepository
            .getMoviesByGenres(suggestedGenres)
            .filter { !movieIds.contains(it.id) }
            .sortedBy { it.id }
            .take(missingRecommendations)
            .map { Recommendation(customer, it, 0.01) }
    }
}