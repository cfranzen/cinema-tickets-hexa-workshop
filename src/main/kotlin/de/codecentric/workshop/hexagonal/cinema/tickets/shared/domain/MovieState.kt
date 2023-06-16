package de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain

enum class MovieState {
    ANNOUNCED,
    PREVIEW,
    IN_THEATER,
    EXPIRED
}