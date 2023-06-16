package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieRecommendationController(
    private val customerRepository: CustomerRepository,
    private val movieRepository: MovieRepository,
    private val datakrakenProperties: DatakrakenProperties,
    private val restTemplateBuilder: RestTemplateBuilder
) {

    companion object {
        private const val MIN_RECOMMENDATIONS = 3
    }

    @GetMapping("/recommendation/{customerId}")
    @Transactional
    fun recommendMoviesToUser(@PathVariable("customerId") customerId: Int): List<RecommendationDTO> {
        val customer = findCustomerById(customerId)
        return calcRecommendations(customer)
    }

    @GetMapping("/recommendation/{customerId}/html", produces = [MediaType.TEXT_HTML_VALUE])
    @Transactional
    fun recommendMoviesToUserInHtml(@PathVariable("customerId") customerId: Int): String {
        val customer = findCustomerById(customerId)
        val recommendations = calcRecommendations(customer)

        return """
            <html>
                <header>                
                    <title>Customer Recommendations</title>
                </header>
                <body>
                <table>
                    <th>
                        <td>Movie ID</td>
                        <td>Title</td>
                        <td>Probability</td>
                    </th>
${
            recommendations.map { printRecommendationInfoAsHtml(it) }.joinToString(separator = "\n")
                .prependIndent("                    ")
        }
                </table>
                </body>
            </html>
        """.trimIndent()
    }

    private fun calcRecommendations(customer: Customer): MutableList<RecommendationDTO> {
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

    private fun findCustomerById(customerId: Int): Customer =
        customerRepository.findById(customerId)
            .orElseThrow { IllegalArgumentException("Could not find customer with ID $customerId") }

    private fun recommendByFavorites(customer: Customer): List<RecommendationDTO> {
        val favoriteMovieIds = customer.data.favorites.map { it.movieId }
        return movieRepository
            .findAllById(favoriteMovieIds)
            .filter { it.state == MovieState.IN_THEATER }
            .map { RecommendationDTO(it.id, 0.5) }
    }

    private fun fillUpByEqualGenre(currentRecommendations: List<RecommendationDTO>): List<RecommendationDTO> {
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieIds = currentRecommendations.map { it.movieId }.toSet()
        val moviesById = movieRepository.findAllById(movieIds).associateBy { it.id }

        val movieFavoriteCount = calculateMoviesFavoriteCount()
        val currentGenres = currentRecommendations.mapNotNull { moviesById[it.movieId]?.genre }.toSet()
        return movieRepository
            .findByGenreIn(currentGenres)
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
        val restTemplate = restTemplateBuilder.rootUri(datakrakenProperties.url).build()
        val response = restTemplate.exchange(
            "/api/?email={email}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Genre>>() {},
            mapOf("email" to customer.email)
        )

        if (response.statusCode.isError || response.body == null || response.body!!.isEmpty()) {
            return emptyList()
        }

        val suggestedGenres = response.body!!
        val missingRecommendations = MIN_RECOMMENDATIONS - currentRecommendations.size
        val movieFavoriteCount = calculateMoviesFavoriteCount()
        return movieRepository
            .findByGenreIn(suggestedGenres)
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

    private fun printRecommendationInfoAsHtml(recommendation: RecommendationDTO): String {
        val movie = movieRepository.findById(recommendation.movieId)
            .orElseThrow { IllegalStateException("Could not find movie for ID ${recommendation.movieId}") }

        return """
            <tr>
                <td>${movie.id}</td>
                <td>${movie.title}</td>
                <td>${recommendation.probability}</td>
            </tr>
        """.trimIndent()
    }

}

data class RecommendationDTO(
    val movieId: Int,
    val probability: Double
)
