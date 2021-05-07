package github.abedurftig

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.inject.Inject

@MicronautTest
class MicronautDataIdExampleTest : DatabaseTest() {

    @Inject
    private lateinit var exampleEntityRepository: ExampleEntityRepository

    @Test
    fun `saved entity should have id from db sequence`() {

        Assertions.assertTrue(exampleEntityRepository.count() == 0L)

        val exampleEntity = ExampleEntity(Long.MAX_VALUE, "Example entity")
        val persistedExampleEntity = exampleEntityRepository.save(exampleEntity)

        Assertions.assertEquals(Long.MAX_VALUE, exampleEntity.id)
        Assertions.assertNotEquals(Long.MAX_VALUE, persistedExampleEntity.id)

        val persistedAgainExampleEntity = exampleEntityRepository.save(exampleEntity)

        Assertions.assertNotEquals(persistedExampleEntity.id, persistedAgainExampleEntity.id)
    }

    @Test
    fun `update entity by referencing DB id`() {

        Assertions.assertTrue(exampleEntityRepository.count() == 0L)

        val exampleEntity = ExampleEntity(Long.MAX_VALUE, "Example entity")
        val persistedEntity = exampleEntityRepository.save(exampleEntity)

        val toBeUpdated = ExampleEntity(persistedEntity.id, "This is an updated name")
        exampleEntityRepository.update(toBeUpdated)
        val updatedOne = exampleEntityRepository.findById(persistedEntity.id).get()

        Assertions.assertEquals(toBeUpdated, updatedOne)
    }
}

@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
interface ExampleEntityRepository : CrudRepository<ExampleEntity, Long>

@MappedEntity("example_entity")
data class ExampleEntity(
    @field:Id
    @GeneratedValue(GeneratedValue.Type.SEQUENCE, ref = "example_entity_id_seq")
    val id: Long = Long.MAX_VALUE,
    val name: String
)

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DatabaseTest : TestPropertyProvider {

    companion object {

        private val LOG = LoggerFactory.getLogger(MicronautDataIdExampleTest::class.java)

        private const val DB_USER = "example-user"
        private const val DB_PASS = "example-pass"
        private const val DB_NAME = "example-db"
        private const val DB_PORT = 5432

        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:12").apply {
            withDatabaseName(DB_NAME)
            withUsername(DB_USER)
            withPassword(DB_PASS)
            withExposedPorts(DB_PORT)
        }
    }

    override fun getProperties(): MutableMap<String, String> {

        val host = container.host
        val port = container.getMappedPort(DB_PORT)
        val url = "jdbc:postgresql://$host:$port/$DB_NAME?loggerLevel=OFF"

        LOG.info("Running Postgres container at '$url'")

        return mutableMapOf(
            "datasources.default.url" to url,
            "datasources.default.username" to DB_USER,
            "datasources.default.password" to DB_PASS,
            "datasources.default.driverClassName" to "org.postgresql.Driver",
        )
    }
}
