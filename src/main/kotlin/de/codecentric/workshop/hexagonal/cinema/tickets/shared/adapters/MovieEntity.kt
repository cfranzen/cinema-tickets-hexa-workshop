package de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.MovieState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("movie")
internal data class MovieEntity(
    @Id
    val id: Int = 0,
    val title: String,
    val genre: Genre,
    val description: String,
    val posterId: String,
    val state: MovieState
)
