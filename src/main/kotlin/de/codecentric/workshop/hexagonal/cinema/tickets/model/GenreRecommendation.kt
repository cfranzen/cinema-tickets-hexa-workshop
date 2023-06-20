package de.codecentric.workshop.hexagonal.cinema.tickets.model

import java.util.*

data class DatakrakenCustomerData(val data: List<CustomerDataEntry>)

data class CustomerDataEntry(
    val name: String,
    val mail: String,
    val timeOfCinemaVisit: Date,
    val movie: String,
    val genres: List<String>?,
)
