package de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain

import de.codecentric.workshop.hexagonal.cinema.tickets.shared.domain.Genre.ACTION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenreTest {

    @Test
    fun `finds genre by lowercase name`() {
        // given
        val genreName = ACTION.name.lowercase()

        // when
        val result = Genre.findByName(genreName)

        // then
        assertThat(result).isEqualTo(ACTION)
    }

    @Test
    fun `finds genre by upercase name`() {
        // given
        val genreName = ACTION.name.uppercase()

        // when
        val result = Genre.findByName(genreName)

        // then
        assertThat(result).isEqualTo(ACTION)
    }

    @Test
    fun `returns null when genre not found`() {
        // given
        val unknownGenre = "unknownGenre"

        // when
        val result = Genre.findByName(unknownGenre)

        // then
        assertThat(result).isNull()
    }



}