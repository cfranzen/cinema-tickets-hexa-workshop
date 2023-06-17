package de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.BookingRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.CustomerRepository
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.ports.MovieRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Service
class BookingService(
    private val clock: Clock,
    private val movieRepository: MovieRepository,
    private val customerRepository: CustomerRepository,
    private val bookingRepository: BookingRepository
) {

    companion object {
        private val ZONE_ID = ZoneId.of("Europe/Berlin")
    }

    fun bookScreening(request: BookingRequest): Booking {
        if (!customerRepository.customerExists(request.customerId)) {
            throw IllegalArgumentException("Could not find customer with id ${request.customerId}")
        }

        val screening = listAvailableScreenings().firstOrNull { it.id == request.screeningId }
            ?: throw IllegalArgumentException("Could not find screening with id ${request.screeningId}")

        val booking = bookingRepository.save(
            Booking(
                customerId = request.customerId,
                movieId = screening.movieId,
                startTime = screening.startTime,
                seats = request.seats,
                bookedAt = clock.instant()
            )
        )
        return booking
    }

    fun listAvailableScreenings(): List<Screening> {
        val moviePreviews = movieRepository.getAllMoviePreviews()
        val moviesInTheater = movieRepository.getAllMoviesInTheater()

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