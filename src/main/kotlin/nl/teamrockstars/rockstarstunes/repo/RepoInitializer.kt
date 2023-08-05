package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.inmemory.RockTunesRepositoryInMemory
import nl.teamrockstars.rockstarstunes.repo.jpa.ArtistRepository
import nl.teamrockstars.rockstarstunes.repo.jpa.RockTunesRepositoryJpa
import nl.teamrockstars.rockstarstunes.repo.jpa.SongRepository
import nl.teamrockstars.rockstarstunes.repo.jpa.toJpaArtist
import nl.teamrockstars.rockstarstunes.repo.jpa.toJpaSong
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader


@Configuration
class RepoInitializer(
    private val resourceLoader: ResourceLoader,
    @Value("\${database.initialize:true}") private val initializeDb: Boolean,
) {

    @Bean
    @Profile("jpa")
    fun initializeJpaRepoFromFile(
        songRepository: SongRepository,
        artistRepository: ArtistRepository,
    ): RockTunesRepository {
        if (initializeDb) {
            val artists: MutableList<Artist> = readArtists()
            val songs: MutableList<Song> = readSongs()
            val missingArtists = ArtistCreator().createAndAddMissingArtists(songs.toList(), artists.toList())
            artistRepository.saveAll(artists.map { it.toJpaArtist() })
            artistRepository.saveAll(missingArtists.map { it.toJpaArtist() })
            songRepository.saveAll(songs.map { it.toJpaSong() })
        }
        return RockTunesRepositoryJpa(artistRepository, songRepository)
    }

    @Bean
    @ConditionalOnMissingBean
    fun initializeInMemoryRepoFromFile(): RockTunesRepository =
        if (initializeDb) {
            val artists: MutableList<Artist> = readArtists()
            val songs: MutableList<Song> = readSongs()
            val missingArtists = ArtistCreator().createAndAddMissingArtists(songs.toList(), artists.toList())
            artists.addAll(missingArtists)
            RockTunesRepositoryInMemory(artists, songs)
        } else RockTunesRepositoryInMemory(mutableListOf(), mutableListOf())

    private fun readSongs(): MutableList<Song> =
        RepoDataDeserializer().deserializeUniqueSongsJson(
            resourceLoader.getResource(
                "classpath:static/json/songs.json"
            ).inputStream
        )

    private fun readArtists(): MutableList<Artist> =
        RepoDataDeserializer().deserializeUniqueArtistsJson(
            resourceLoader.getResource(
                "classpath:static/json/artists.json"
            ).inputStream
        )

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