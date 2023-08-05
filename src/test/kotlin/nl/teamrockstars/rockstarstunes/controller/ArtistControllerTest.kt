package nl.teamrockstars.rockstarstunes.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
class ArtistControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    lateinit var rockTunesRepository: RockTunesRepository
    private val mapper = jacksonObjectMapper()

    private val artist = Artist(1L, "3 Doors Down")

    @Test
    fun `Get band by id`() {
        every { rockTunesRepository.findArtistByIdOrNull(artist.id) } returns artist

        mockMvc.perform(get("/rocktunes-api/artist/{id}", artist.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(artist.name))
    }

    @Test
    fun `Get NOT_FOUND response when band is not found`() {
        val unknownArtistId = 2L
        every { rockTunesRepository.findArtistByIdOrNull(unknownArtistId) } returns null

        mockMvc.perform(get("/rocktunes-api/artist/{id}", 2))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Add new band`() {
        every { rockTunesRepository.saveArtist(artist) } returns Result.success(artist)

        mockMvc.perform(
            post("/rocktunes-api/artist")
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(artist.name))
    }

    @Test
    fun `Add new band, is not successful when band exists`() {
        val errorMsg = "Duplicate"
        every { rockTunesRepository.saveArtist(artist) } returns Result.failure(DuplicateResourceException(errorMsg))

        mockMvc.perform(
            post("/rocktunes-api/artist")
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isConflict)
            .andExpect(content().string(errorMsg))
    }

    @Test
    fun `Update band by id`() {
        every { rockTunesRepository.updateArtist(artist.id, artist) } returns Result.success(artist)

        mockMvc.perform(
            put("/rocktunes-api/artists/{id}", 1)
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(artist.name))
    }

    @Test
    fun `Update band by id, is not successful when band doesn't exist`() {
        val errorMsg = "Not Found"
        every {
            rockTunesRepository.updateArtist(
                artist.id,
                artist
            )
        } returns Result.failure(ResourceNotFoundException(errorMsg))

        mockMvc.perform(
            put("/rocktunes-api/artists/{id}", artist.id)
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().string(errorMsg))
    }

    @Test
    fun `Update band by id, is not successful when id on path doesn't match id on body`() {
        mockMvc.perform(
            put("/rocktunes-api/artists/{id}", 2)
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Delete band by id`() {
        justRun { rockTunesRepository.deleteArtistByIdOrNull(artist.id) }

        mockMvc.perform(delete("/rocktunes-api/artist/{id}", artist.id))
            .andExpect(status().isAccepted())
    }

    @Test
    fun `Delete band by id, is not successful when band does not exist`() {
        val errorMsg = "Not Found"
        every { rockTunesRepository.deleteArtistByIdOrNull(artist.id) } returns Result.failure(ResourceNotFoundException(
            errorMsg
        ))

        mockMvc.perform(delete("/rocktunes-api/artist/{id}", artist.id))
            .andExpect(status().isNotFound)
            .andExpect(content().string(errorMsg))
    }

    @Test
    fun `Delete band by id, fails with Unprocessable Entity`() {
        val errorMsg = "Unprocessable Entity"
        every { rockTunesRepository.deleteArtistByIdOrNull(artist.id) } returns Result.failure(UnprocessableEntityException(
            errorMsg
        ))

        mockMvc.perform(delete("/rocktunes-api/artist/{id}", artist.id))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().string(errorMsg))
    }

    @Test
    fun getAllBands() {
        every { rockTunesRepository.findAllArtists() } returns listOf(artist)

        mockMvc.perform(get("/rocktunes-api/artists"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[0].name").value(artist.name))
    }
}