package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Screening
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@RestController
class BookingController(
    private val clock: Clock,
    private val movieRepository: MovieRepository
) {

    companion object {
        private val ZONE_ID = ZoneId.of("Europe/Berlin")
    }

    @GetMapping("/screenings")
    fun findScreenings(): ResponseEntity<List<Screening>> {
        return ResponseEntity.ok().body(listScreenings())
    }

    private fun listScreenings(): List<Screening> {
        val moviePreviews = movieRepository.findByState(MovieState.PREVIEW)
        val moviesInTheater = movieRepository.findByState(MovieState.IN_THEATER)

        val lastThursday = findLastThursday()
        val previewTimeslots = findPreviewTimeslots(lastThursday)
        val normalTimeslots = findNormalTimeslots(lastThursday)

        var id = 0
        return previewTimeslots.flatMap { timeslot ->
            moviePreviews.map { movie ->
                id += 1
                Screening(
                    id = id,
                    movieId = movie.id,
                    startTime = timeslot
                )
            }
        }.plus(
            normalTimeslots.flatMap { timeslot ->
                moviesInTheater.map { movie ->
                    id += 1
                    Screening(
                        id = id,
                        movieId = movie.id,
                        startTime = timeslot
                    )
                }
            })
    }

    private fun findLastThursday(): LocalDate {
        var current = clock.instant().atZone(ZONE_ID).toLocalDate()
        for (i in 0L..6L) {
            current = current.minusDays(i)
            if (current.dayOfWeek == DayOfWeek.THURSDAY) {
                return current
            }
        }
        throw IllegalStateException("Unable to determine last thursday. This should never happen.")
    }

    private fun findPreviewTimeslots(startDate: LocalDate) =
        // Previews are only on Wednesdays in the evening
        listOf(
            startDate.plusDays(6).atTime(19, 0)
        )

    private fun findNormalTimeslots(startDate: LocalDate) =
        // There are two timeslots for screenings each day
        (0..6).flatMap {
            listOf(
                startDate.plusDays(it.toLong()).atTime(14, 30),
                startDate.plusDays(it.toLong()).atTime(20, 0),
            )
        }
}