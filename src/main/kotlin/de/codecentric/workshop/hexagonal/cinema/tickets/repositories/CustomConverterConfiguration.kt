package de.codecentric.workshop.hexagonal.cinema.tickets.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.codecentric.workshop.hexagonal.cinema.tickets.model.CustomerData
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions


@Configuration
class CustomConverterConfiguration(
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun jdbcCustomConversions(): JdbcCustomConversions? {
        return JdbcCustomConversions(
            listOf(
                CustomerHistoryToStringConverter(objectMapper),
                StringToCustomerHistory(objectMapper),
            )
        )
    }
}


@WritingConverter
class CustomerHistoryToStringConverter(
    private val objectMapper: ObjectMapper
) : Converter<CustomerData, String> {

    override fun convert(source: CustomerData): String {
        return objectMapper.writeValueAsString(source)
    }
}

@ReadingConverter
class StringToCustomerHistory(
    private val objectMapper: ObjectMapper
) : Converter<String, CustomerData> {

    override fun convert(source: String): CustomerData {
        return objectMapper.readValue(source)
    }
}

