package de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.adapters.inbound

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Customer
import de.codecentric.workshop.hexagonal.cinema.tickets.recommendations.domain.RecommendationService
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.SpringMovieRepository
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieRecommendationController(
    private val customerRepository: CustomerRepository,
    private val springMovieRepository: SpringMovieRepository,
    private val recommendationService: RecommendationService
) {

    @GetMapping("/recommendation/{customerId}")
    @Transactional
    fun recommendMoviesToUser(@PathVariable("customerId") customerId: Int): List<RecommendationDTO> {
        val customer = findCustomerById(customerId)
        return recommendationService.calcRecommendations(customer)
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

    private fun findCustomerById(customerId: Int): Customer =
        customerRepository.findById(customerId)
            .orElseThrow { IllegalArgumentException("Could not find customer with ID $customerId") }

    private fun printRecommendationInfoAsHtml(recommendation: RecommendationDTO): String {
        val movie = springMovieRepository.findById(recommendation.movieId)
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
