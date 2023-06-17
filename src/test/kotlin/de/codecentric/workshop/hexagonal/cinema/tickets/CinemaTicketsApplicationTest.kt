package de.codecentric.workshop.hexagonal.cinema.tickets

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
class CinemaTicketsApplicationTest {

    @Test
    fun contextLoads() {
    }

    companion object {
        @Container
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .withReuse(true)
            .waitingFor(Wait.forListeningPort())

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }
}
