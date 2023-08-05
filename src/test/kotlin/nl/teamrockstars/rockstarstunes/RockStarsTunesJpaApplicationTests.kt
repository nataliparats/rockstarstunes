package nl.teamrockstars.rockstarstunes

import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("jpa")
class RockStarsTunesJpaApplicationTests (@Autowired val rockTunesRepository: RockTunesRepository) {

	companion object {
		@Container
		@ServiceConnection
		private val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
	}

	@Test
	fun contextLoads() {
	}

	@Test
	fun initializesInMemoryDatabase() {
		assertEquals(1002, rockTunesRepository.findAllArtists().size)
		assertEquals(2517, rockTunesRepository.findAllSongs().size)
	}

}
