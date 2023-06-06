package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MoviePosterController {

    @GetMapping("/movie/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: Int): ResponseEntity<String> {
        // fetch movie, which is the poster ID/ url
        // fetch poster
        // return poster

        return ResponseEntity.ok().body("Hello world $movieId")
    }
}