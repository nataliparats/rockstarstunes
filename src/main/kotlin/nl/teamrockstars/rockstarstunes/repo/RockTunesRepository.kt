package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song

class RockTunesRepository(private val artists: MutableList<Artist>, private val songs: MutableList<Song>) {
    fun findArtistByIdOrNull(id: Long): Artist? {
        return artists.find { it.id == id }
    }

    fun findAllArtists(): List<Artist> = artists.toList()

    fun saveArtist(artist: Artist): Result<Artist> {
        return if (artists.firstOrNull { it.id == artist.id } != null) {
            Result.failure(DuplicateResourceException("De band bestaat al"))
        } else {
            artists.add(artist)
            Result.success(artist)
        }
    }

    fun updateArtist(id: Long, artist: Artist): Result<Artist> {
        val storedArtistIndex = artists.indexOfFirst { it.id == id }
        return if (storedArtistIndex != -1) {
            if (artists.find { it.name == artist.name } != null)
                return Result.failure(DuplicateResourceException("De band met naam ${artist.name} bestaat al"))
            artists[storedArtistIndex] = artist
            Result.success(artists[storedArtistIndex])
        } else {
            Result.failure(ResourceNotFoundException("De band bestaat niet"))
        }
    }

    fun deleteArtistByIdOrNull(id: Long): Result<Unit> {
        val artist = artists.find { it.id == id }
        return if (artist != null) {
            if (songs.find { it.artist == artist.name } != null)
                return Result.failure(UnprocessableEntityException("De band wordt geassocieerd met opgeslagen nummers. " +
                        "Verwijder eerst alle bijbehorende nummers en verwijder vervolgens de band"))
            artists.remove(artist)
            Result.success(Unit)
        } else {
            Result.failure(ResourceNotFoundException("De band bestaat niet"))
        }
    }

    fun findSongByIdOrNull(id: Long): Song? =
        songs.find { it.id == id }

    fun saveSong(song: Song): Result<Song> {
        return if (songs.firstOrNull { it.id == song.id } != null) {
            Result.failure(DuplicateResourceException("Het liedje bestaat niet"))
        } else if (artists.firstOrNull { it.name == song.artist } == null) {
            Result.failure(
                UnprocessableEntityException(
                    "De artiest naam bestaat niet in onze repo. Probeer eerst " +
                            "om de artiest toe te voegen"
                )
            )
        } else {
            songs.add(song)
            Result.success(song)
        }
    }

    fun updateSong(id: Long, song: Song): Result<Song> {
        val storedSongIndex = songs.indexOfFirst { it.id == id }
        return if (storedSongIndex != -1) {
            if (artists.find { it.name == song.artist } == null)
                return Result.failure(
                    UnprocessableEntityException(
                        "De artiest met naam ${song.artist} bestaat niet in " +
                                "onze repo. Probeer eerst om de artiest toe te voegen"
                    )
                )
            songs[storedSongIndex] = song
            Result.success(songs[storedSongIndex])
        } else {
            Result.failure(ResourceNotFoundException("Het liedje bestaat niet"))
        }
    }

    fun deleteSongByIdOrNull(id: Long): Result<Unit> {
        val song = songs.find { it.id == id }
        return if (song != null) {
            songs.remove(song)
            Result.success(Unit)
        } else {
            Result.failure(ResourceNotFoundException("Het liedje bestaat niet"))
        }
    }

    fun findAllSongs(): List<Song> = songs.toList()
}

class DuplicateResourceException(msg: String) : RuntimeException(msg)
class ResourceNotFoundException(msg: String) : RuntimeException(msg)
class UnprocessableEntityException(msg: String) : RuntimeException(msg)