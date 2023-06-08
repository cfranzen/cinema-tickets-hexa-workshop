package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import de.codecentric.workshop.hexagonal.cinema.tickets.config.MoviePostersProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Movie
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MovieController(
    private val movieRepository: MovieRepository,
    private val storage: Storage,
    private val properties: MoviePostersProperties
) {

    @PostMapping("/movies")
    fun createNewMovie(@RequestBody request: MovieWithoutIdDTO): ResponseEntity<Movie> {
        val newMovie = request.toMovie()
        val persistedMovie = movieRepository.save(newMovie)
        return ResponseEntity.ok().body(persistedMovie)
    }

    @GetMapping("/movies/{id}")
    fun findMovie(@PathVariable("id") movieId: Int): ResponseEntity<Movie> {
        return movieRepository
            .findById(movieId)
            .map { ResponseEntity.ok().body(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }

    @GetMapping("/movies/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: Int): ResponseEntity<Resource> {
        val movie = movieRepository
            .findById(movieId)
            .orElseThrow { IllegalArgumentException("Could not find movie with ID $movieId") }

        val poster = storage.get(BlobId.of(properties.bucket, movie.posterId))
            ?: return ResponseEntity.notFound().build()

        val content = poster.getContent()
        return ResponseEntity.ok().body(ByteArrayResource(content))
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