package de.dmalo.integration.testcontext

import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import de.dmalo.mediarepository.springconfig.OakRepositoryConfig
import de.dmalo.web.MediaApiController
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@EnableAutoConfiguration
@EnableConfigurationProperties(MediaRepositoryContextProperties::class)
@ComponentScan("de.dmalo.*")
@Import(OakRepositoryConfig::class, MediaApiController::class)
class IntegrationTestContext
