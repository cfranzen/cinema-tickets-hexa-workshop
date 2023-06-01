package de.codecentric.workshop.hexagonal.cinema.tickets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CinemaTicketsApplication

fun main(args: Array<String>) {
	runApplication<CinemaTicketsApplication>(*args)
}
