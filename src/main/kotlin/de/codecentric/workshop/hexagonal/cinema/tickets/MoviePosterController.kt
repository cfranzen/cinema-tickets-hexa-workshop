package de.codecentric.workshop.hexagonal.cinema.tickets

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MoviePosterController {

    @GetMapping("/movie/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: String): ResponseEntity<String> {
        return ResponseEntity.ok().body("Hello world $movieId")
    }
}