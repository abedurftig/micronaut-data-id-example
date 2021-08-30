package github.abedurftig

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class MicronautDataIdExampleTest {

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
