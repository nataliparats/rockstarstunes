package nl.teamrockstars.rockstarstunes.repo

import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.model.Song

interface RockTunesRepository {
    fun findArtistByIdOrNull(id: Long): Artist?
    fun findAllArtists(): List<Artist>
    fun saveArtist(artist: Artist): Result<Artist>
    fun updateArtist(id: Long, artist: Artist): Result<Artist>
    fun deleteArtistByIdOrNull(id: Long): Result<Unit>
    fun findSongByIdOrNull(id: Long): Song?
    fun saveSong(song: Song): Result<Song>
    fun updateSong(id: Long, song: Song): Result<Song>
    fun deleteSongByIdOrNull(id: Long): Result<Unit>
    fun findAllSongs(): List<Song>
    fun findAllSongsByGenre(genre: String): List<Song>
}
class DuplicateResourceException(msg: String) : RuntimeException(msg)
class ResourceNotFoundException(msg: String) : RuntimeException(msg)
class UnprocessableEntityException(msg: String) : RuntimeException(msg)