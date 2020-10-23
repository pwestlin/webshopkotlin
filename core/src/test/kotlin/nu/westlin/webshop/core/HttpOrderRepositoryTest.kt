package nu.westlin.webshop.core

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.test.order1
import nu.westlin.webshop.test.order4
import nu.westlin.webshop.test.orders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

internal class HttpOrderRepositoryTest {

    private lateinit var mockServer: WireMockServer

    private lateinit var repository: HttpOrderRepository

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    // TODO petves: Move to TestUtils
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Suppress("unused")
    @BeforeAll
    fun setup() {
        mockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply { start() }

        repository = HttpOrderRepository(
            WebClientConfiguration().orderServiceWebClient("http://localhost:${mockServer.port()}")
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
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(orders)))
        )

        assertThat(repository.all().toList()).containsExactlyElementsOf(orders)
    }

    @Test
    fun `get order by id that exist`() = runBlocking<Unit> {
        val order = order1
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/${order.id}"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(order)))
        )

        assertThat(repository.get(order.id)).isEqualTo(order)
    }

    @Test
    fun `get order by id that does not exist`() = runBlocking {
        val orderId = 1
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/$orderId"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.notFound())
        )

        assertThat(repository.get(orderId)).isNull()
    }

    @Test
    fun `add a order with an id that does not exist`() = runBlocking {
        val order = order4

        mockServer.stubFor(
            WireMock.post(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(order)))
                .willReturn(WireMock.ok())
        )

        repository.add(order)

        mockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(order)))
        )
    }
}