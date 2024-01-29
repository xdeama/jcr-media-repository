package de.dmalo

import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(MediaRepositoryContextProperties::class)
open class RestApplication

fun main(args: Array<String>) {
    runApplication<RestApplication>(*args)
}
