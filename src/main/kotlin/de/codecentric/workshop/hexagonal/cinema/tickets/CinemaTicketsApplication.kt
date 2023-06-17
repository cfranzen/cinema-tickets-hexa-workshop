package de.codecentric.workshop.hexagonal.cinema.tickets

import de.codecentric.workshop.hexagonal.cinema.tickets.recommendation.config.DatakrakenProperties
import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.config.MoviePostersProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    MoviePostersProperties::class,
    DatakrakenProperties::class
)
class CinemaTicketsApplication

fun main(args: Array<String>) {
    runApplication<CinemaTicketsApplication>(*args)
}
