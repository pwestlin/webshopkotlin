package nu.westlin.webshop.core

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.webshop.test.cookie
import nu.westlin.webshop.test.engineOil
import nu.westlin.webshop.test.products
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

internal class HttpProductRepositoryTest {

    private lateinit var mockServer: WireMockServer

    private lateinit var repository: HttpProductRepository

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Suppress("unused")
    @BeforeAll
    fun setup() {
        mockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply { start() }

        repository = HttpProductRepository(
            WebClientConfiguration().webClientBuilder()
                .baseUrl("http://localhost:${mockServer.port()}")
                .build()
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
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(products)))
        )

        assertThat(repository.all().toList()).containsExactlyElementsOf(products)
    }

    @Test
    fun `get product by id that exist`() = runBlocking<Unit> {
        val product = engineOil
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/${product.id}"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.okJson(objectMapper.writeValueAsString(product)))
        )

        assertThat(repository.get(product.id)).isEqualTo(product)
    }

    @Test
    fun `get product by id that does not exist`() = runBlocking {
        val productId = 1
        mockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/$productId"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.notFound())
        )

        assertThat(repository.get(productId)).isNull()
    }

    @Test
    fun `add a product with an id that does not exist`() = runBlocking {
        val product = cookie

        mockServer.stubFor(
            WireMock.post(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(product)))
                .willReturn(WireMock.ok())
        )

        repository.add(product)

        mockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlPathEqualTo("/"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(EqualToPattern(objectMapper.writeValueAsString(product)))
        )
    }
}