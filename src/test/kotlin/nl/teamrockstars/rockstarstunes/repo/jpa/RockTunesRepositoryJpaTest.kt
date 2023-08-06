package nl.teamrockstars.rockstarstunes.repo.jpa

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import nl.teamrockstars.rockstarstunes.repo.jpa.model.Artist as JpaArtist
import nl.teamrockstars.rockstarstunes.repo.jpa.model.Song as JpaSong


@SpringBootTest(properties = ["database.initialize=false"])
@Testcontainers
@ActiveProfiles("jpa")
class RockTunesRepositoryJpaTest(
    @Autowired private val artistRepository: ArtistRepository,
    @Autowired private val songRepository: SongRepository,
) {

    private val repository = RockTunesRepositoryJpa(artistRepository, songRepository)

    companion object {
        @Container
        @ServiceConnection
        private val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
    }

    @AfterEach
    fun clearDatabase() {
        artistRepository.deleteAll()
        songRepository.deleteAll()
    }


    @Test
    fun `Find artist by id, returns 1 artist`() {
        val savedArtist = JpaArtist(1, "artist name")
        artistRepository.save(savedArtist)

        val result: Artist? = repository.findArtistByIdOrNull(savedArtist.id)

        assertEquals(Artist(savedArtist.id, savedArtist.name), result)
    }

    @Test
    fun `Find artist by id, returns null if artist is not found`() {
        val savedArtist = JpaArtist(1, "artist name")
        artistRepository.save(savedArtist)

        val result: Artist? = repository.findArtistByIdOrNull(2L)

        assertNull(result)
    }

    @Test
    fun findAllArtists() {
        val savedArtist = JpaArtist(1, "artist name")
        artistRepository.save(savedArtist)

        val result: List<Artist> = repository.findAllArtists()

        assertEquals(listOf(savedArtist.toArtist()), result)
    }

    @Test
    fun `Add artist successfully`() {
        val artist = Artist(1L, "artist name")

        val result: Result<Artist> = repository.saveArtist(artist)

        assertEquals(Result.success(artist), result)
        assertTrue(artistRepository.findById(artist.id).isPresent)
    }

    @Test
    fun `Add artist that already exists, returns result failure with duplicate resource exception`() {
        val artist = Artist(1L, "3 Doors Down")
        repository.saveArtist(artist)

        val result = repository.saveArtist(artist)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
    }

    @Test
    fun `Update artist`() {
        val savedArtist = JpaArtist(1, "artist name")
        artistRepository.save(savedArtist)

        val toBeUpdatedArtist = Artist(savedArtist.id, "new artist name")
        val result: Result<Artist> = repository.updateArtist(toBeUpdatedArtist.id, toBeUpdatedArtist)

        assertEquals(Result.success(toBeUpdatedArtist), result)
        assertEquals(1, artistRepository.findAll().size)
    }

    @Test
    fun `Update artist that does not exist, returns result failure with not found exception`() {
        val updatedArtist = Artist(145634563456L, "3 Doors Up")

        val result = repository.updateArtist(updatedArtist.id, updatedArtist)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(0, artistRepository.findAll().size)
    }

    @Test
    fun `Update artist when in the repo another artist has the same name, returns result failure duplicate exception`() {
        val sameNameArtist = JpaArtist(1L, "Same Name Artist")
        artistRepository.save(sameNameArtist)
        val artistToBeUpdated = JpaArtist(2L, "A Name")
        artistRepository.save(artistToBeUpdated)
        val updatedArtist = Artist(2L, "Same Name Artist")

        val result = repository.updateArtist(updatedArtist.id, updatedArtist)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
        val artistInDb = artistRepository.findById(updatedArtist.id).get()
        assertEquals(artistToBeUpdated.id, artistInDb.id)
        assertEquals(artistToBeUpdated.name, artistInDb.name)
    }

    @Test
    fun `Delete artist`() {
        val savedArtist = JpaArtist(1, "artist name")
        artistRepository.save(savedArtist)

        val result: Result<Unit> = repository.deleteArtistByIdOrNull(savedArtist.id)

        assertTrue(result.isSuccess)
        assertFalse(artistRepository.findById(savedArtist.id).isPresent)
    }

    @Test
    fun `Delete artist that does not exist, returns result failure with not found exception`() {
        val artist = JpaArtist(1L, "3 Doors Down")
        artistRepository.save(artist)

        val result = repository.deleteArtistByIdOrNull(2L)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(1, artistRepository.findAll().size)
    }

    @Test
    fun `Delete artist when one ore more songs are associated to this artist, returns result failure with unprocessable content exception`() {
        val artist = JpaArtist(1L, "3 Doors Down")
        artistRepository.save(artist)
        val song = JpaSong(
            1L, "Test Song", 2016, "3 Doors Down",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)

        val result = repository.deleteArtistByIdOrNull(1L)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
        assertEquals(1, artistRepository.findAll().size)
    }

    @Test
    fun `Find song by id, returns 1 song`() {
        val jpaSong = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(jpaSong)

        val result = repository.findSongByIdOrNull(jpaSong.id)

        assertEquals(jpaSong.toSong(), result)
    }

    @Test
    fun `Find song by id, returns null if song is not found`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)

        val result = repository.findSongByIdOrNull(2L)

        assertEquals(null, result)
    }

    @Test
    fun `Add song successfully`() {
        val artist = JpaArtist(1L, "Test Artist")
        artistRepository.save(artist)
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )

        val result = repository.saveSong(song)

        assertEquals(song, result.getOrNull())
        assertTrue(songRepository.existsById(song.id))
    }

    @Test
    fun `Add song when artist name is not in the database, returns failure with unprocessable content exception`() {
        val artist = JpaArtist(1L, "Test Artist")
        artistRepository.save(artist)
        val song = Song(
            1L, "Test Song", 2016, "Another Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )

        val result = repository.saveSong(song)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
        assertFalse(songRepository.existsById(song.id))
    }

    @Test
    fun `Add a song that already exists, returns result failure with duplicate resource exception`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)

        val result = repository.saveSong(song.toSong())

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
    }

    @Test
    fun `Update song`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)
        val artist = JpaArtist(1L, "Test Artist")
        artistRepository.save(artist)

        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Test Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val result = repository.updateSong(updatedSong.id, updatedSong)

        assertEquals(updatedSong, result.getOrNull())
        assertEquals(1, songRepository.findAll().size)
    }

    @Test
    fun `Update song that does not exist, returns result failure with not found exception`() {
        val artist = JpaArtist(1L, "Test Artist")
        artistRepository.save(artist)

        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Test Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val result = repository.updateSong(updatedSong.id, updatedSong)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(0, songRepository.findAll().size)
    }

    @Test
    fun `Update song when provided artist doesn't exist, returns failure with unprocessable content exception`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)
        val artist = JpaArtist(1L, "Test Artist")
        artistRepository.save(artist)

        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Not Found Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val result = repository.updateSong(updatedSong.id, updatedSong)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
    }

    @Test
    fun `Delete song`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)

        val result = repository.deleteSongByIdOrNull(1L)

        assertEquals(true, result.isSuccess)
        assertFalse(songRepository.existsById(song.id))
    }

    @Test
    fun `Delete song that does not exist, returns result failure with not found exception`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        songRepository.save(song)

        val nonExistentId = 2L
        val result = repository.deleteSongByIdOrNull(nonExistentId)

        assertEquals(true, result.isFailure)
        Assertions.assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertFalse(songRepository.existsById(nonExistentId))
    }

    @Test
    fun findAllSongs() {
        val song1 = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val song2 = JpaSong(
            2L, "Test Song 2", 2017, "Test Artist 2",
            "song2", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Album 2"
        )
        songRepository.save(song1)
        songRepository.save(song2)

        val result = repository.findAllSongs()

        assertEquals(listOf(song1.toSong(), song2.toSong()), result)
    }

    @Test
    fun `Find all songs by genre`() {
        val requestedGenre = "Metal"
        val song1 = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, requestedGenre, "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val song2 = JpaSong(
            2L, "Test Song 2", 2017, "Test Artist 2",
            "song2", 289, 212208, "Something Else", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Album 2"
        )
        val song3 = JpaSong(
            2L, "Test Song 2", 2017, "Test Artist 2",
            "song2", 289, 212208, requestedGenre, "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Album 2"
        )
        songRepository.saveAll(listOf(song1, song2, song3))

        val result = repository.findAllSongsByGenre(requestedGenre)

        assertEquals(listOf(song1.toSong(), song3.toSong()), result)
    }

    @Test
    fun `Find all songs by genre, returns empty list if no songs match`() {
        val song = JpaSong(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )

        songRepository.saveAll(listOf(song))

        val result = repository.findAllSongsByGenre("Another Genre")

        assertTrue(result.isEmpty())
    }
}