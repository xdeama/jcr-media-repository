package de.dmalo.integration.testcontext

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@SpringBootTest(classes = [IntegrationTestContext::class])
@ContextConfiguration(
    initializers = [
        MediaLibraryTestContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
annotation class IntegrationTestConfiguration
