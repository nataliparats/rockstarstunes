package nl.teamrockstars.rockstarstunes.repo.jpa

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.repo.jpa.model.Artist as JpaArtist
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.jpa.model.Song as JpaSong
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

interface ArtistRepository: JpaRepository<JpaArtist, Long> {
    fun findByName(name: String): JpaArtist?
}

interface SongRepository: JpaRepository<JpaSong, Long> {
    fun findByArtist(name: String): List<JpaSong>

    fun findByGenre(genre: String): List<JpaSong>
}

fun JpaArtist.toArtist() = Artist(this.id, this.name)

fun Artist.toJpaArtist() = JpaArtist(this.id, this.name)

fun JpaSong.toSong() = Song(this.id, this.name, this.year, this.artist, this.shortname, this.bpm, this.duration,
    this.genre, this.spotifyId, this.album)

fun Song.toJpaSong() = JpaSong(this.id, this.name, this.year, this.artist, this.shortname, this.bpm, this.duration,
    this.genre, this.spotifyId, this.album)

class RockTunesRepositoryJpa( private val artistRepository: ArtistRepository,
    private val songRepository: SongRepository) : RockTunesRepository {

    override fun findArtistByIdOrNull(id: Long): Artist? = artistRepository.findByIdOrNull(id)?.toArtist()

    override fun findAllArtists(): List<Artist> = artistRepository.findAll().map { it.toArtist() }

    override fun saveArtist(artist: Artist): Result<Artist> {
        if (artistRepository.existsById(artist.id)){
            return Result.failure(DuplicateResourceException("De band bestaat al"))
        }
        artistRepository.save(artist.toJpaArtist())
        return Result.success(artist)
    }


    override fun updateArtist(id: Long, artist: Artist): Result<Artist> {
        if (!artistRepository.existsById(id))
            return Result.failure(ResourceNotFoundException("De band bestaat niet"))
        if (artistRepository.findByName(artist.name) != null)
            return Result.failure(DuplicateResourceException("De band met naam ${artist.name} bestaat al"))
        artistRepository.save(artist.toJpaArtist())
        return Result.success(artist)
    }

    override fun deleteArtistByIdOrNull(id: Long): Result<Unit> {
        if (!artistRepository.existsById(id)){
            return Result.failure(ResourceNotFoundException("De band bestaat niet"))
        }
        val artist = artistRepository.findById(id).get()
        if (songRepository.findByArtist(artist.name).isNotEmpty()) {
            return Result.failure(
                UnprocessableEntityException(
                    "De band wordt geassocieerd met opgeslagen nummers. " +
                            "Verwijder eerst alle bijbehorende nummers en verwijder vervolgens de band"
                )
            )
        }
        artistRepository.deleteById(id)
        return Result.success(Unit)
    }

    override fun findSongByIdOrNull(id: Long): Song? = songRepository.findByIdOrNull(id)?.toSong()

    override fun saveSong(song: Song): Result<Song> {
        if (songRepository.existsById(song.id)){
            return Result.failure(DuplicateResourceException("De band bestaat al"))
        }
        if (artistRepository.findByName(song.artist) == null) {
            return Result.failure(
                UnprocessableEntityException(
                    "De artiest naam bestaat niet in onze repo. Probeer eerst " +
                            "om de artiest toe te voegen"
                )
            )
        }
        songRepository.save(song.toJpaSong())
        return Result.success(song)
    }

    override fun updateSong(id: Long, song: Song): Result<Song> {
        if (!songRepository.existsById(id))
            return Result.failure(ResourceNotFoundException("Het liedje bestaat niet"))
        if (artistRepository.findByName(song.artist) == null)
            return Result.failure(
                UnprocessableEntityException(
                    "De artiest met naam ${song.artist} bestaat niet in " +
                            "onze repo. Probeer eerst om de artiest toe te voegen"
                )
            )
        songRepository.save(song.toJpaSong())
        return Result.success(song)
    }

    override fun deleteSongByIdOrNull(id: Long): Result<Unit> {
        if (!songRepository.existsById(id))
            return Result.failure(ResourceNotFoundException("Het liedje bestaat niet"))
        songRepository.deleteById(id)
        return Result.success(Unit)
    }

    override fun findAllSongs(): List<Song> = songRepository.findAll().map { it.toSong() }

    override fun findAllSongsByGenre(genre: String): List<Song> =
        songRepository.findByGenre(genre).map { it.toSong() }

}

