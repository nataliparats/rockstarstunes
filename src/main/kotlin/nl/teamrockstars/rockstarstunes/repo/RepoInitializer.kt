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