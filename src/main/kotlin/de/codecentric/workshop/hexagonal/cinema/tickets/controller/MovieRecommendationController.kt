package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieRecommendationController {

    @GetMapping("/recommendation/{customerId}")
    fun recommendMoviesToUser(@PathVariable("customerId") userId: Int): List<Recommendation> {
        // fetch user, with history of his visited movies and rating or favorites
        // fetch current movies
        // calculate which movies the user could like
        // return json

        return emptyList()
    }
}

data class Recommendation(
    val movieId: Int,
    val probability: Double
)