package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import de.codecentric.workshop.hexagonal.cinema.tickets.config.MoviePostersProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieEntity
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.MovieSpringRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
internal class MovieController(
    private val movieSpringRepository: MovieSpringRepository,
    private val storage: Storage,
    private val properties: MoviePostersProperties
) {

    @PostMapping("/movies")
    fun createNewMovie(@RequestBody request: MovieWithoutIdDTO): ResponseEntity<MovieEntity> {
        val newMovie = request.toMovie()
        val persistedMovie = movieSpringRepository.save(newMovie)
        return ResponseEntity.ok().body(persistedMovie)
    }

    @GetMapping("/movies/{id}")
    fun findMovie(@PathVariable("id") movieId: Int): ResponseEntity<MovieEntity> {
        return movieSpringRepository
            .findById(movieId)
            .map { ResponseEntity.ok().body(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }

    @GetMapping("/movies/{id}/poster")
    fun getMoviePoster(@PathVariable("id") movieId: Int): ResponseEntity<Resource> {
        val movie = movieSpringRepository
            .findById(movieId)
            .orElseThrow { IllegalArgumentException("Could not find movie with ID $movieId") }

        val poster = storage.get(BlobId.of(properties.bucket, movie.posterId))
            ?: return ResponseEntity.notFound().build()

        val content = poster.getContent()
        return ResponseEntity.ok().body(ByteArrayResource(content))
    }
}

internal data class MovieWithoutIdDTO(
    val title: String,
    val genre: Genre,
    val description: String,
    val posterId: String,
    val state: MovieState
) {
    fun toMovie() = MovieEntity(
        title = this.title,
        genre = this.genre,
        description = this.description,
        posterId = this.posterId,
        state = this.state
    )
}