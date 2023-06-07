package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieRecommendationController(
    private val customerRepository: CustomerRepository,
    private val movieRepository: MovieRepository
) {

    companion object {
        private const val MIN_RECOMMENDATIONS = 3
    }

    @GetMapping("/recommendation/{customerId}")
    fun recommendMoviesToUser(@PathVariable("customerId") customerId: Int): List<RecommendationDTO> {
        val customer = customerRepository.findById(customerId)
            .orElseThrow { IllegalArgumentException("Could not find customer with ID $customerId") }

        val recommendations = mutableListOf<RecommendationDTO>()
        recommendations.addAll(recommendByFavorites(customer))

        if (recommendations.size < MIN_RECOMMENDATIONS) {
            recommendations.addAll(fillUpByEqualGenre(recommendations))

        }
        return recommendations
    }

    private fun recommendByFavorites(customer: Customer): List<RecommendationDTO> {
        val favoriteMovieIds = customer.data.favoriteMovies.map { it.movieId }
        return movieRepository
            .findAllById(favoriteMovieIds)
            .filter { it.state == MovieState.IN_THEATER }
            .map { RecommendationDTO(it.id, 0.5) }
    }

    private fun fillUpByEqualGenre(currentRecommendations: List<RecommendationDTO>): List<RecommendationDTO> {
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieIds = currentRecommendations.map { it.movieId }.toSet()
        val moviesById = movieRepository.findAllById(movieIds).associateBy { it.id }

        val movieFavoriteCount = customerRepository
            .findAll()
            .flatMap { customer -> customer.data.favoriteMovies.map { it.movieId } }
            .groupingBy { it }
            .eachCount()

        val currentGenres = currentRecommendations.mapNotNull { moviesById[it.movieId]?.genre }.toSet()
        return movieRepository
            .findByGenreIn(currentGenres)
            .filter { !movieIds.contains(it.id) }
            .sortedBy { movieFavoriteCount.getOrDefault(it.id, 0) }
            .reversed()
            .take(missingRecommendations)
            .map { RecommendationDTO(it.id, 0.05) }
    }
}

data class RecommendationDTO(
    val movieId: Int,
    val probability: Double
)
