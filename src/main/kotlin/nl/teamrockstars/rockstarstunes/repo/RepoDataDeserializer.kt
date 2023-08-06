package nl.teamrockstars.rockstarstunes.repo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class RepoDataDeserializer {

    fun deserializeUniqueArtistsJson(json: InputStream): MutableList<Artist> {
        val mapper = jacksonObjectMapper()
        val artistsJson = mapper.readValue<List<ArtistJson>>(json)

        return artistsJson
            .map { it.toArtist() }
            .distinctBy { it.name }
            .toMutableList()
    }

    fun deserializeUniqueSongsJson(json: InputStream): MutableList<Song> {
        val mapper = jacksonObjectMapper()
        val songsJson = mapper.readValue<List<SongJson>>(json)

        return songsJson
            .map { it.toSong() }
            .toMutableList()
    }
}

private data class ArtistJson(
    val Id: Int,
    val Name: String
) {
    fun toArtist() = Artist(
        id = Id.toLong(),
        name = Name
    )
}

private data class SongJson(
    val Id: Int,
    val Name: String,
    val Year: Int,
    val Artist: String,
    val Shortname: String,
    val Bpm: Int,
    val Duration: Long,
    val Genre: String,
    val SpotifyId: String?,
    val Album: String?
) {
    fun toSong() = Song(
        id = Id.toLong(),
        name = Name,
        year = Year,
        artist = Artist,
        shortname = Shortname,
        bpm = Bpm,
        duration = Duration,
        genre = Genre,
        spotifyId = SpotifyId,
        album = Album
    )
}

