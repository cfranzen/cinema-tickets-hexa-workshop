package de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.repositories.PosterRepository
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
    private val posterRepository: PosterRepository
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

        val posterId = movie.posterId
        val poster = posterRepository.findPosterById(posterId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().body(ByteArrayResource(poster))
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