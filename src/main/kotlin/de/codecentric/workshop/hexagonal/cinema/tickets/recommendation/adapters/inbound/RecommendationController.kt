package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.adapters.inbound

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.Recommendation
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.domain.RecommendationService
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerSpringRepository
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
internal class MovieRecommendationController(
    private val customerSpringRepository: CustomerSpringRepository,
    private val recommendationService: RecommendationService
) {
    @GetMapping("/recommendation/{customerId}")
    @Transactional
    fun recommendMoviesToUser(@PathVariable("customerId") customerId: Int): List<RecommendationDTO> {
        val customer = findCustomerById(customerId)
        return recommendationService
            .calcRecommendations(customer)
            .map { RecommendationDTO(movieId = it.movie.id, probability = it.probability) }
    }

    @GetMapping("/recommendation/{customerId}/html", produces = [MediaType.TEXT_HTML_VALUE])
    @Transactional
    fun recommendMoviesToUserInHtml(@PathVariable("customerId") customerId: Int): String {
        val customer = findCustomerById(customerId)
        val recommendations = recommendationService.calcRecommendations(customer)

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

    private fun findCustomerById(customerId: Int): Customer {
        val entity = customerSpringRepository.findById(customerId)
            .orElseThrow { IllegalArgumentException("Could not find customer with ID $customerId") }
        return Customer(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            favoriteMovieIds = entity.data.favorites.map { it.movieId }
        )

    }

    private fun printRecommendationInfoAsHtml(recommendation: Recommendation): String {
        return """
            <tr>
                <td>${recommendation.movie.id}</td>
                <td>${recommendation.movie.title}</td>
                <td>${recommendation.probability}</td>
            </tr>
        """.trimIndent()
    }

}

internal data class RecommendationDTO(
    val movieId: Int,
    val probability: Double
)
