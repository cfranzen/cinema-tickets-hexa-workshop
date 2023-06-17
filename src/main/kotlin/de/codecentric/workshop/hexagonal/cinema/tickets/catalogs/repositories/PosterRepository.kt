package de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.repositories

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.config.MoviePostersProperties
import org.springframework.stereotype.Repository

@Repository
class PosterRepository(
    private val storage: Storage,
    private val properties: MoviePostersProperties
) {

    fun findPosterById(posterId: String): ByteArray? =
        storage.get(BlobId.of(properties.bucket, posterId))?.getContent()
}