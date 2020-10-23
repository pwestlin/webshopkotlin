package nu.westlin.webshop.core

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.test.customers
import nu.westlin.webshop.test.jen
import nu.westlin.webshop.test.steve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

internal class HttpCustomerRepositoryTest {

    private lateinit var mockServer: WireMockServer

    private lateinit var repository: HttpCustomerRepository

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private val objectMapper = jacksonObjectMapper()

    @Suppress("unused")
    @BeforeAll
    fun setup() {
        mockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply { start() }

        repository = HttpCustomerRepository(
            WebClientConfiguration().customerServiceWebClient("http://localhost:${mockServer.port()}")
        )
    }

    @AfterEach
    fun rensaMockar() {
        logger.debug("All requests for mockServer:\n${mockServer.allServeEvents.joinToString("\n") { it.request.toString() }}")
        mockServer.resetAll()
    }

    @AfterAll
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun `get all`() = runBlocking<Unit> {
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(customers)))
        )

        assertThat(repository.all().toList()).containsExactlyElementsOf(customers)
    }

    @Test
    fun `get customer by id that exist`() = runBlocking<Unit> {
        val customer = jen
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/${jen.id}"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(jen)))
        )

        assertThat(repository.get(customer.id)).isEqualTo(customer)
    }

    @Test
    fun `get customer by id that does not exist`() = runBlocking {
        val customerId = 1
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/$customerId"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.notFound())
        )

        assertThat(repository.get(customerId)).isNull()
    }

    @Test
    fun `add a customer with an id that does not exist`() = runBlocking {
        val customer = steve

        mockServer.stubFor(
            WireMock.post(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(customer)))
                .willReturn(WireMock.ok())
        )

        repository.add(steve)

        mockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(customer)))
        )
    }
}