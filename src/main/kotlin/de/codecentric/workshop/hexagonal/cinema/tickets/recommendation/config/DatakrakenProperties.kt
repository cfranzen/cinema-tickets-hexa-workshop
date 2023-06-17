package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "customer.datakraken")
class DatakrakenProperties @ConstructorBinding constructor(
    val url: String,
)
