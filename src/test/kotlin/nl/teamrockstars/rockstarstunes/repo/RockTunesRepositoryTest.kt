package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class RockTunesRepositoryTest {

    @Test
    fun `Find artist by id, returns 1 artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.findArtistByIdOrNull(artist.id)

        assertEquals(artist, result)
    }

    @Test
    fun `Find artist by id, returns null if artist is not found`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.findArtistByIdOrNull(2L)

        assertEquals(null, result)
    }

    @Test
    fun `Add artist successfully`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf())

        val result = rockTunesRepository.saveArtist(artist)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(artist, result.getOrNull())
        assertEquals(1, artists.size)
    }

    @Test
    fun `Add artist that already exists, returns result failure with duplicate resource exception`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.saveArtist(artist)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
    }

    @Test
    fun `Update artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())
        val updatedArtist = Artist(1L, "3 Doors Up")

        val result = rockTunesRepository.updateArtist(updatedArtist.id, updatedArtist)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(updatedArtist, result.getOrNull())
        assertEquals(1, artists.size)
    }

    @Test
    fun `Update artist that does not exist, returns result failure with not found exception`() {
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf())
        val updatedArtist = Artist(1L, "3 Doors Up")

        val result = rockTunesRepository.updateArtist(updatedArtist.id, updatedArtist)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(0, artists.size)
    }

    @Test
    fun `Update artist when in the repo another artist has the same name, returns result failure duplicate exception`() {
        val sameNameArtist = Artist(1L, "Same Name Artist")
        val artistToBeUpdated = Artist(2L, "A Name")
        val rockTunesRepository = RockTunesRepository(mutableListOf(sameNameArtist, artistToBeUpdated), mutableListOf())
        val updatedArtist = Artist(2L, "Same Name Artist")

        val result = rockTunesRepository.updateArtist(updatedArtist.id, updatedArtist)
        val notUpdatedArtist = rockTunesRepository.findArtistByIdOrNull(2L)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
        assertEquals(artistToBeUpdated, notUpdatedArtist)
    }

    @Test
    fun `Delete artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.deleteArtistByIdOrNull(1L)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(true, result.isSuccess)
        assertEquals(0, artists.size)
    }

    @Test
    fun `Delete artist that does not exist, returns result failure with not found exception`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.deleteArtistByIdOrNull(2L)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(1, artists.size)
    }

    @Test
    fun `Delete artist when one ore more songs are associated to this artist, returns result failure with unprocessable content exception`() {
        val artist = Artist(1L, "3 Doors Down")
        val song = Song(
            1L, "Test Song", 2016, "3 Doors Down",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepository.deleteArtistByIdOrNull(1L)
        val artists = rockTunesRepository.findAllArtists()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
        assertEquals(1, artists.size)
    }

    @Test
    fun `Find song by id, returns 1 song`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepository.findSongByIdOrNull(song.id)

        assertEquals(song, result)
    }

    @Test
    fun `Find song by id, returns null if song is not found`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepository.findSongByIdOrNull(2L)

        assertEquals(null, result)
    }

    @Test
    fun `Add song successfully`() {
        val artist = Artist(1L, "Test Artist")
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.saveSong(song)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(song, result.getOrNull())
        assertEquals(1, songs.size)
    }

    @Test
    fun `Add song when artist name is not in the database, returns failure with unprocessable content exception`() {
        val artist = Artist(1L, "Test Artist")
        val song = Song(
            1L, "Test Song", 2016, "Another Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.saveSong(song)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
        assertEquals(0, songs.size)
    }

    @Test
    fun `Add a song that already exists, returns result failure with duplicate resource exception`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepository.saveSong(song)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
    }

    @Test
    fun `Update song`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Test Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val artist = Artist(1L, "Test Artist")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepository.updateSong(updatedSong.id, updatedSong)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(updatedSong, result.getOrNull())
        assertEquals(1, songs.size)
    }

    @Test
    fun `Update song that does not exist, returns result failure with not found exception`() {
        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Test Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val artist = Artist(1L, "Test Artist")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepository.updateSong(updatedSong.id, updatedSong)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(0, songs.size)
    }

    @Test
    fun `Update song when in the repo another song has the same artist name, is successfully updated`() {
        val artist = Artist(1L, "Test Artist")
        val sameNameSong = Song(
            1L, "Test Song", 2016, artist.name,
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val songToBeUpdated = Song(
            2L, "To be updated Test Song", 2017, artist.name,
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val rockTunesRepository =
            RockTunesRepository(mutableListOf(artist), mutableListOf(sameNameSong, songToBeUpdated))
        val update = Song(
            2L, "Updated Test Song", 2017, artist.name,
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )

        val result = rockTunesRepository.updateSong(update.id, update)
        val  updatedSong= rockTunesRepository.findSongByIdOrNull(2L)
        assertEquals(Result.success(updatedSong!!), result)
        assertEquals(update, updatedSong)
    }

    @Test
    fun `Update song when provided artist doesn't exist, returns failure with unprocessable content exception`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val updatedSong = Song(
            1L, "Updated Test Song!", 2017, "Not Found Artist",
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )
        val artist = Artist(1L, "Test Artist")
        val rockTunesRepository = RockTunesRepository(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepository.updateSong(updatedSong.id, updatedSong)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
    }

    @Test
    fun `Delete song`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepository.deleteSongByIdOrNull(1L)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(true, result.isSuccess)
        assertEquals(0, songs.size)
    }

    @Test
    fun `Delete song that does not exist, returns result failure with not found exception`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepository = RockTunesRepository(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepository.deleteSongByIdOrNull(2L)
        val songs = rockTunesRepository.findAllSongs()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(1, songs.size)
    }
}