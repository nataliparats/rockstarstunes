package nl.teamrockstars.rockstarstunes

import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RockStarsTunesApplicationTests (@Autowired val rockTunesRepository: RockTunesRepository) {

	@Test
	fun contextLoads() {
	}

	@Test
	fun initializesInMemoryDatabase() {
		assertEquals(rockTunesRepository.findAllArtists().size, 1002)
		assertEquals(rockTunesRepository.findAllSongs().size, 2517)
	}

}
