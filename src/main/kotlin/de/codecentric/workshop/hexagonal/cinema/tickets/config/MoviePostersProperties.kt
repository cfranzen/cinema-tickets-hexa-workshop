package de.codecentric.workshop.hexagonal.cinema.tickets.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "movie.posters")
class MoviePostersProperties @ConstructorBinding constructor(
    val bucket: String,
)
