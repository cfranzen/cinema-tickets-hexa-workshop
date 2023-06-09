package de.codecentric.workshop.hexagonal.cinema.tickets.model

import org.springframework.data.annotation.Id

data class Movie(
    @Id
    val id: Int = 0,
    val title: String,
    val genre: Genre,
    val description: String,
    val posterId: String,
    val state: MovieState
)

enum class Genre {
    ACTION,
    COMEDY,
    DRAMA,
    FANTASY,
    HORROR,
    MYSTERY,
    ROMANCE,
    THRILLER,
    WESTERN;

    companion object {
        fun findByName(name: String): Genre? {
            return Genre.values().find { it.name == name.uppercase() }
        }
    }
}

enum class MovieState {
    ANNOUNCED,
    PREVIEW,
    IN_THEATER,
    EXPIRED
}