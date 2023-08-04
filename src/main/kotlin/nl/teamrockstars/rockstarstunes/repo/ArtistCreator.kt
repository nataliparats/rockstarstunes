package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import org.springframework.stereotype.Component

@Component
class ArtistCreator {
    fun createAndAddMissingArtists(songs: List<Song>, artists: List<Artist>): List<Artist> {
        val missingArtistNames = songs.map { it.artist }
            .filterNot { it in artists.map { it.name } }
            .distinct()
        val maxExistingId = artists.maxOfOrNull { it.id }!!
        return missingArtistNames.mapIndexed { index, artistName -> Artist(maxExistingId + index + 1, artistName) }
    }
}