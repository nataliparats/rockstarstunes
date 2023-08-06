package nl.teamrockstars.rockstarstunes.repo.inmemory

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class RockTunesRepositoryInMemoryTest {

    @Test
    fun `Find artist by id, returns 1 artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.findArtistByIdOrNull(artist.id)

        assertEquals(artist, result)
    }

    @Test
    fun `Find artist by id, returns null if artist is not found`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.findArtistByIdOrNull(2L)

        assertNull(result)
    }

    @Test
    fun `Add artist successfully`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf())

        val result = rockTunesRepositoryInMemory.saveArtist(artist)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

        assertEquals(artist, result.getOrNull())
        assertEquals(1, artists.size)
    }

    @Test
    fun `Add artist that already exists, returns result failure with duplicate resource exception`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.saveArtist(artist)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
    }

    @Test
    fun `Update artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())
        val updatedArtist = Artist(1L, "3 Doors Up")

        val result = rockTunesRepositoryInMemory.updateArtist(updatedArtist.id, updatedArtist)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

        assertEquals(updatedArtist, result.getOrNull())
        assertEquals(1, artists.size)
    }

    @Test
    fun `Update artist that does not exist, returns result failure with not found exception`() {
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf())
        val updatedArtist = Artist(1L, "3 Doors Up")

        val result = rockTunesRepositoryInMemory.updateArtist(updatedArtist.id, updatedArtist)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(0, artists.size)
    }

    @Test
    fun `Update artist when in the repo another artist has the same name, returns result failure duplicate exception`() {
        val sameNameArtist = Artist(1L, "Same Name Artist")
        val artistToBeUpdated = Artist(2L, "A Name")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(sameNameArtist, artistToBeUpdated), mutableListOf())
        val updatedArtist = Artist(2L, "Same Name Artist")

        val result = rockTunesRepositoryInMemory.updateArtist(updatedArtist.id, updatedArtist)
        val notUpdatedArtist = rockTunesRepositoryInMemory.findArtistByIdOrNull(updatedArtist.id)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(DuplicateResourceException::class.java)
        assertEquals(artistToBeUpdated, notUpdatedArtist)
    }

    @Test
    fun `Delete artist`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.deleteArtistByIdOrNull(1L)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

        assertEquals(true, result.isSuccess)
        assertEquals(0, artists.size)
    }

    @Test
    fun `Delete artist that does not exist, returns result failure with not found exception`() {
        val artist = Artist(1L, "3 Doors Down")
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.deleteArtistByIdOrNull(2L)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.deleteArtistByIdOrNull(1L)
        val artists = rockTunesRepositoryInMemory.findAllArtists()

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.findSongByIdOrNull(song.id)

        assertEquals(song, result)
    }

    @Test
    fun `Find song by id, returns null if song is not found`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.findSongByIdOrNull(2L)

        assertEquals(null, result)
    }

    @Test
    fun `Add song successfully`() {
        val artist = Artist(1L, "Test Artist")
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.saveSong(song)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.saveSong(song)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.saveSong(song)

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.updateSong(updatedSong.id, updatedSong)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf())

        val result = rockTunesRepositoryInMemory.updateSong(updatedSong.id, updatedSong)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

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
        val rockTunesRepositoryInMemory =
            RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf(sameNameSong, songToBeUpdated))
        val update = Song(
            2L, "Updated Test Song", 2017, artist.name,
            "updatedsong", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Updated Album"
        )

        val result = rockTunesRepositoryInMemory.updateSong(update.id, update)
        val  updatedSong= rockTunesRepositoryInMemory.findSongByIdOrNull(2L)
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
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(artist), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.updateSong(updatedSong.id, updatedSong)

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(UnprocessableEntityException::class.java)
    }

    @Test
    fun `Delete song`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.deleteSongByIdOrNull(1L)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

        assertEquals(true, result.isSuccess)
        assertEquals(0, songs.size)
    }

    @Test
    fun `Delete song that does not exist, returns result failure with not found exception`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))

        val result = rockTunesRepositoryInMemory.deleteSongByIdOrNull(2L)
        val songs = rockTunesRepositoryInMemory.findAllSongs()

        assertEquals(true, result.isFailure)
        assertThat(result.exceptionOrNull()).isInstanceOf(ResourceNotFoundException::class.java)
        assertEquals(1, songs.size)
    }

    @Test
    fun `Find all songs by genre and year`() {
        val requestedGenre = "Metal"
        val requestedYear = 2016
        val song1 = Song(
            1L, "Test Song", 2015, "Test Artist",
            "song", 100, 197350, requestedGenre, "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val song2 = Song(
            2L, "Test Song 2", 2017, "Test Artist 2",
            "song2", 289, 212208, "Something Else", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Album 2"
        )
        val song3 = Song(
            2L, "Test Song 2", 2017, "Test Artist 2",
            "song2", 289, 212208, requestedGenre, "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Album 2"
        )
        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song1, song2, song3))

        val result = rockTunesRepositoryInMemory.findAllSongsByGenreAndYear(requestedGenre, requestedYear)

        assertEquals(listOf(song3), result)
    }

    @Test
    fun `Find all songs by genre, returns empty list if no songs match`() {
        val song = Song(
            1L, "Test Song", 2016, "Test Artist",
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )

        val rockTunesRepositoryInMemory = RockTunesRepositoryInMemory(mutableListOf(), mutableListOf(song))


        val result = rockTunesRepositoryInMemory.findAllSongsByGenreAndYear("Another Genre", null)

        assertTrue(result.isEmpty())
    }
}