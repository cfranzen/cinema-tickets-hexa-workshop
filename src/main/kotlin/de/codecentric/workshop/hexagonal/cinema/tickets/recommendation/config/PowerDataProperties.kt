package de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "customer.powerdata")
class PowerDataProperties @ConstructorBinding constructor(
    val url: String,
)
