package nl.teamrockstars.rockstarstunes.controller

import io.swagger.v3.oas.annotations.tags.Tag
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import java.lang.RuntimeException

@RestController
@RequestMapping("/rocktunes-api")
@Tag(name = "Songs")
class SongController(
    @Autowired private val rockTunesRepository: RockTunesRepository
) {

    @GetMapping("/song/{id}")
    fun getSong(@PathVariable id: Long): ResponseEntity<Song> {
        val song = rockTunesRepository.findSongByIdOrNull(id)
        return if (song != null) ResponseEntity(song, HttpStatus.OK)
        else ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/song")
    fun addSong(@RequestBody song: Song): ResponseEntity<Song> {
        val result = rockTunesRepository.saveSong(song)
        when {
            result.isSuccess -> return ResponseEntity(result.getOrNull(), HttpStatus.CREATED)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @PutMapping("/songs/{id}")
    fun updateSong(@PathVariable id: Long, @RequestBody song: Song): ResponseEntity<Song> {
        validateMatchingIdWithSongId(id, song)
        val result = rockTunesRepository.updateSong(id, song)
        when {
            result.isSuccess -> return ResponseEntity(result.getOrNull(), HttpStatus.OK)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @DeleteMapping("/song/{id}")
    fun deleteSongById(@PathVariable id: Long): ResponseEntity<Unit> {
        val result = rockTunesRepository.deleteSongByIdOrNull(id)
        return when {
            result.isSuccess -> ResponseEntity(HttpStatus.ACCEPTED)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @GetMapping("/songs")
    fun getAllSongs(): ResponseEntity<List<Song>> {
        val songs = rockTunesRepository.findAllSongs()
        return ResponseEntity(songs, HttpStatus.OK)
    }


    @ExceptionHandler
    fun handleExceptions(exception: Exception) = when (exception) {
        is DuplicateResourceException -> ResponseEntity(exception.message, HttpStatus.CONFLICT)
        is ResourceNotFoundException -> ResponseEntity(exception.message, HttpStatus.NOT_FOUND)
        else -> ResponseEntity.badRequest().body(exception.message)
    }

    private fun validateMatchingIdWithSongId(id: Long, song: Song) {
        if (id != song.id) throw RuntimeException("Het id in het path en het id in de body zijn niet hetzelfde.")
    }

}