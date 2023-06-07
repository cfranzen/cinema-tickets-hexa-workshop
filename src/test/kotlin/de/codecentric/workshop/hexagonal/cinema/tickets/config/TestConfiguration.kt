package de.codecentric.workshop.hexagonal.cinema.tickets.config

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Paths
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
                        .newBuilder(BlobId.of(properties.bucket.toString(), it.fileName.toString()))
                        .build()
                    storage.create(blobInfo, Files.readAllBytes(it))
                }
        }
        return storage
    }
}