package nl.teamrockstars.rockstarstunes.controller

import io.swagger.v3.oas.annotations.tags.Tag
import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
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

@RestController
@RequestMapping("/rocktunes-api")
@Tag(name = "Bands")
class ArtistController(
    private val rockTunesRepository: RockTunesRepository
) {

    @GetMapping("/artist/{id}")
    fun getArtist(@PathVariable id: Long): ResponseEntity<Artist> {
        val artist = rockTunesRepository.findArtistByIdOrNull(id)
        return if (artist != null) ResponseEntity(artist, HttpStatus.OK)
        else ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/artist")
    fun addArtist(@RequestBody artist: Artist): ResponseEntity<Artist> {
        val result = rockTunesRepository.saveArtist(artist)
        when {
            result.isSuccess -> return ResponseEntity(result.getOrNull(), HttpStatus.CREATED)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @PutMapping("/artists/{id}")
    fun updateArtist(@PathVariable id: Long, @RequestBody artist: Artist): ResponseEntity<Artist> {
        validateMatchingIdWithArtistId(id, artist)
        val result = rockTunesRepository.updateArtist(id, artist)
        when {
            result.isSuccess -> return ResponseEntity(result.getOrNull(), HttpStatus.OK)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @DeleteMapping("/artist/{id}")
    fun deleteArtistById(@PathVariable id: Long): ResponseEntity<Unit> {
        val result = rockTunesRepository.deleteArtistByIdOrNull(id)
        return when {
            result.isSuccess -> ResponseEntity(HttpStatus.ACCEPTED)
            else -> throw result.exceptionOrNull()!!
        }
    }

    @GetMapping("/artists")
    fun getAllArtists(): ResponseEntity<List<Artist>> {
        val artists = rockTunesRepository.findAllArtists()
        return ResponseEntity(artists, HttpStatus.OK)
    }


    @ExceptionHandler
    fun handleExceptions(exception: Exception) = when (exception) {
        is DuplicateResourceException -> ResponseEntity(exception.message, HttpStatus.CONFLICT)
        is ResourceNotFoundException -> ResponseEntity(exception.message, HttpStatus.NOT_FOUND)
        is UnprocessableEntityException -> ResponseEntity(exception.message, HttpStatus.UNPROCESSABLE_ENTITY)
        else -> ResponseEntity.badRequest().body(exception.message)
    }

    private fun validateMatchingIdWithArtistId(id: Long, artist: Artist) {
        if (id != artist.id) throw RuntimeException("Het id in het path en het id in de body zijn niet hetzelfde.")
    }

}