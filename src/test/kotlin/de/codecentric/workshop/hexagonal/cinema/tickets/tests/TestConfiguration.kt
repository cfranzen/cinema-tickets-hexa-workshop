package de.codecentric.workshop.hexagonal.cinema.tickets.tests

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import de.codecentric.workshop.hexagonal.cinema.tickets.catalogs.config.MoviePostersProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Clock
import kotlin.io.path.isRegularFile


@Configuration
class TestConfiguration {

    @Bean
    fun storage(properties: MoviePostersProperties): Storage {
        val storage = LocalStorageHelper.getOptions().service

        val dir = Paths.get("posters")
        Files.walk(dir).use { stream ->
            stream.filter { it.isRegularFile() }
                .forEach {
                    val blobInfo = BlobInfo
                        .newBuilder(BlobId.of(properties.bucket, it.fileName.toString()))
                        .build()
                    storage.create(blobInfo, Files.readAllBytes(it))
                }
        }
        return storage
    }

    @Bean
    fun fixedClock(): Clock = Clock.fixed(NOW, ZONE)
}