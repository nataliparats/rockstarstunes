package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader


@Configuration
class RepoInitializer {

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    @Bean
    fun initializeRepoFromFile(): RockTunesRepository {
        val artists: MutableList<Artist> = RepoDataDeserializer().deserializeUniqueArtistsJson(
            resourceLoader.getResource(
                "classpath:static/json/artists.json"
            ).inputStream
        )
        val songs: MutableList<Song> = RepoDataDeserializer().deserializeUniqueSongsJson(
            resourceLoader.getResource(
                "classpath:static/json/songs.json"
            ).inputStream
        )
        val missingArtists = ArtistCreator().createAndAddMissingArtists(songs.toList(), artists.toList())
        artists.addAll(missingArtists)
        return RockTunesRepositoryInMemory(artists, songs)
    }

}
class ArtistCreator {
    fun createAndAddMissingArtists(songs: List<Song>, artists: List<Artist>): List<Artist> {
        val missingArtistNames = songs.map { it.artist }
            .filterNot { it in artists.map { it.name } }
            .distinct()
        val maxExistingId = artists.maxOfOrNull { it.id }!!
        return missingArtistNames.mapIndexed { index, artistName -> Artist(maxExistingId + index + 1, artistName) }
    }
}