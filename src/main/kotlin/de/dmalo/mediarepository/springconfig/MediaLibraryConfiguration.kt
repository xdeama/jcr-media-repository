package de.dmalo.mediarepository.springconfig

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan("de.dmalo.mediarepository")
@Configuration
@EnableConfigurationProperties(MediaRepositoryContextProperties::class)
open class MediaLibraryConfiguration
