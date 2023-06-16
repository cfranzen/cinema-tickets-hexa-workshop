package de.codecentric.workshop.hexagonal.cinema.tickets.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.codecentric.workshop.hexagonal.cinema.tickets.shared.adapters.CustomerEntityData
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
                CustomerDataToStringConverter(objectMapper),
                StringToCustomerHistory(objectMapper),
            )
        )
    }
}


@WritingConverter
internal class CustomerDataToStringConverter(
    private val objectMapper: ObjectMapper
) : Converter<CustomerEntityData, String> {

    override fun convert(source: CustomerEntityData): String {
        return objectMapper.writeValueAsString(source)
    }
}

@ReadingConverter
internal class StringToCustomerHistory(
    private val objectMapper: ObjectMapper
) : Converter<String, CustomerEntityData> {

    override fun convert(source: String): CustomerEntityData {
        return objectMapper.readValue(source)
    }
}

