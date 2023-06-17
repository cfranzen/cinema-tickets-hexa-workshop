package de.codecentric.workshop.hexagonal.cinema.tickets.booking.adapters.inbound

import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Booking
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.BookingRequest
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.BookingService
import de.codecentric.workshop.hexagonal.cinema.tickets.booking.domain.Screening
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
internal class BookingController(
    private val bookingService: BookingService
) {

    @PostMapping("/bookings")
    fun createNewBooking(@RequestBody request: BookingRequest): ResponseEntity<Booking> {
        val booking = bookingService.bookScreening(request)
        return ResponseEntity.ok().body(booking)
    }

    @GetMapping("/screenings")
    fun findScreenings(): ResponseEntity<List<Screening>> {
        val screenings = bookingService.listAvailableScreenings()
        return ResponseEntity.ok().body(screenings)
    }


}

