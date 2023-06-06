package de.codecentric.workshop.hexagonal.cinema.tickets

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieRecommendationController {

    @GetMapping("/movie/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: String): ResponseEntity<String> {
        // fetch user, with history of his visited movies and rating or favorites
        // fetch current movies
        // calculate which movies the user could like
        // return json

        return ResponseEntity.ok().body("Hello world $movieId")
    }
}