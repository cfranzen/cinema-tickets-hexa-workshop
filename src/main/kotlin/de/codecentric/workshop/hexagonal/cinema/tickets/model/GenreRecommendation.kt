package de.codecentric.workshop.hexagonal.cinema.tickets.model

data class DatakrakenCustomerData(val data: List<CustomerDataEntry>)

data class CustomerDataEntry(
    val name: String,
    val mail: String,
    val movie: String,
    val genres: List<String>?,
)
