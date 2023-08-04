package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class ArtistCreatorTest {

    @Test
    fun `Given a list of Songs and a list of Artists, when a song is associated with an artist not in Artists list, create Artist`() {
        val artist = Artist(1L, "Test Artist")
        val toBeCreatedArtist = "To be created Artist"
        val song = Song(
            1L, "Test Song", 2016, artist.name,
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val songWithMissingArtist = Song(
            2L, "Missing Artist Song", 2017, toBeCreatedArtist,
            "missingartsist", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Missing Album"
        )
        val songs = listOf(song, songWithMissingArtist)

        val result = ArtistCreator().createAndAddMissingArtists(songs, listOf(artist))

        assertEquals(listOf(Artist(2L, toBeCreatedArtist)), result)
    }

    @Test
    fun `Given a list of Songs and a list of Artists, when a song is associated with an artist not in Artists list, do not create Artist if already created from previous missing Artist`() {
        val artist = Artist(1L, "Test Artist")
        val toBeCreatedArtist = "To be created Artist"
        val song = Song(
            1L, "Test Song", 2016, artist.name,
            "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
        )
        val songWithMissingArtist = Song(
            2L, "Missing Artist Song ", 2017, toBeCreatedArtist,
            "missingartist", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Missing Album"
        )
        val songWithSameMissingArtist = Song(
            3L, "Same Missing Artist Song", 2017, toBeCreatedArtist,
            "samemissingartist", 289, 212208, "Metal", "6Ud9fOJQ9ZO2qnsMFPiJsh", "Test Missing Album"
        )
        val songs = listOf(song, songWithMissingArtist, songWithSameMissingArtist)

        val result = ArtistCreator().createAndAddMissingArtists(songs, listOf(artist))

        assertEquals(listOf(Artist(2L, toBeCreatedArtist)), result)
    }
}