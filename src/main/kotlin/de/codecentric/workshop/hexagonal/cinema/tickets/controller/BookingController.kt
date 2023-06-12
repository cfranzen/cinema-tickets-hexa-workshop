package de.codecentric.workshop.hexagonal.cinema.tickets.controller

import de.codecentric.workshop.hexagonal.cinema.tickets.model.Booking
import de.codecentric.workshop.hexagonal.cinema.tickets.model.MovieState
import de.codecentric.workshop.hexagonal.cinema.tickets.model.Screening
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.BookingRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.repositories.MovieRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@RestController
class BookingController(
    private val clock: Clock,
    private val movieRepository: MovieRepository,
    private val bookingRepository: BookingRepository,
    private val customerRepository: CustomerRepository
) {

    companion object {
        private val ZONE_ID = ZoneId.of("Europe/Berlin")
    }

    @PostMapping("/bookings")
    fun createNewBooking(@RequestBody request: BookingDTO): ResponseEntity<Booking> {
        val screening = listScreenings().firstOrNull { it.id == request.screeningId }
            ?: throw IllegalArgumentException("Could not find screening with id ${request.screeningId}")

        val customer = customerRepository.findById(request.customerId)
            .orElseThrow { IllegalArgumentException("Could not find customer with id ${request.customerId}") }

        val booking = bookingRepository.save(
            Booking(
                customerId = customer.id,
                movieId = screening.movieId,
                startTime = screening.startTime,
                seats = request.seats,
                bookedAt = clock.instant()
            )
        )
        return ResponseEntity.ok().body(booking)
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
        val today = clock.instant().atZone(ZONE_ID).toLocalDate()
        for (i in 0L..6L) {
            val thursdayCandidate = today.minusDays(i)
            if (thursdayCandidate.dayOfWeek == DayOfWeek.THURSDAY) {
                return thursdayCandidate
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

data class BookingDTO(
    val customerId: Int,
    val screeningId: Int,
    val seats: Int,
) {
    init {
        if (seats <= 0) {
            throw IllegalArgumentException("Seats must be a positive integer but $seats was given")
        }
    }
}