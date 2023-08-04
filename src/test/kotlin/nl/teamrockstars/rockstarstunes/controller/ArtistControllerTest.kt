package nl.teamrockstars.rockstarstunes.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import nl.teamrockstars.rockstarstunes.model.Artist
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
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
        every { rockTunesRepository.findArtistByIdOrNull(1L) } returns artist

        mockMvc.perform(get("/rocktunes-api/artist/{id}", 1))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(artist.name))
    }

    @Test
    fun `Get NOT_FOUND response when band is not found`() {
        every { rockTunesRepository.findArtistByIdOrNull(2L) } returns null

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
        every { rockTunesRepository.saveArtist(artist) } returns Result.failure(DuplicateResourceException("Duplicate"))

        mockMvc.perform(
            post("/rocktunes-api/artist")
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isConflict)
            .andExpect(content().string("Duplicate"))
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
        every {
            rockTunesRepository.updateArtist(
                artist.id,
                artist
            )
        } returns Result.failure(ResourceNotFoundException("Not Found"))

        mockMvc.perform(
            put("/rocktunes-api/artists/{id}", 1)
                .content(mapper.writeValueAsString(artist))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().string("Not Found"))
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
        justRun { rockTunesRepository.deleteArtistByIdOrNull(1L) }

        mockMvc.perform(delete("/rocktunes-api/artist/{id}", 1))
            .andExpect(status().isAccepted())
    }

    @Test
    fun `Delete band by id, is not successful when band does not exist`() {
        every { rockTunesRepository.deleteArtistByIdOrNull(1L) } returns Result.failure(ResourceNotFoundException("Not Found"))

        mockMvc.perform(delete("/rocktunes-api/artist/{id}", 1))
            .andExpect(status().isNotFound)
            .andExpect(content().string("Not Found"))
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