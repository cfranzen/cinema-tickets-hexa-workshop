package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieController(
    private val movieRepository: MovieRepository
) {

    @PostMapping("/movies")
    fun createNewMovie(@RequestBody request: MovieWithoutIdDTO): ResponseEntity<Movie> {
        val newMovie = request.toMovie()
        val persistedMovie = movieRepository.save(newMovie)
        return ResponseEntity.ok().body(persistedMovie)
    }

    @GetMapping("/movies/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: Int): ResponseEntity<String> {
        // fetch movie, which is the poster ID/ url
        // fetch poster
        // return poster

        return ResponseEntity.ok().body("Hello world $movieId")
    }
}

data class MovieWithoutIdDTO(
    val title: String,
    val genre: Genre,
    val description: String,
    val posterId: String,
    val state: MovieState
) {
    fun toMovie() = Movie(
        title = this.title,
        genre = this.genre,
        description = this.description,
        posterId = this.posterId,
        state = this.state
    )
}