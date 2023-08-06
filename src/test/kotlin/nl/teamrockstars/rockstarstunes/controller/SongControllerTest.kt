package nl.teamrockstars.rockstarstunes.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import nl.teamrockstars.rockstarstunes.model.Song
import nl.teamrockstars.rockstarstunes.repo.DuplicateResourceException
import nl.teamrockstars.rockstarstunes.repo.ResourceNotFoundException
import nl.teamrockstars.rockstarstunes.repo.RockTunesRepository
import nl.teamrockstars.rockstarstunes.repo.UnprocessableEntityException
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest
class SongControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    lateinit var rockTunesRepository: RockTunesRepository
    private val mapper = jacksonObjectMapper()

    private val song = Song(
        1L, "Test Song", 2016, "Test Artist",
        "song", 100, 197350, "Metal", "1LkjMNCu16QUwHJbzTqPnR", "Test Album"
    )

    @Test
    fun `Get song by Id`() {
        every { rockTunesRepository.findSongByIdOrNull(song.id) } returns song

        mockMvc.perform(MockMvcRequestBuilders.get("/rocktunes-api/song/{id}", song.id))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(song.name))
    }

    @Test
    fun `Get NOT_FOUND response when song is not found`() {
        val unknownSongId = 2L
        every { rockTunesRepository.findSongByIdOrNull(unknownSongId) } returns null

        mockMvc.perform(MockMvcRequestBuilders.get("/rocktunes-api/song/{id}", unknownSongId))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `Add new song`() {
        every { rockTunesRepository.saveSong(song) } returns Result.success(song)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/rocktunes-api/song")
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(song.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(song.year))
            .andExpect(MockMvcResultMatchers.jsonPath("$.artist").value(song.artist))
            .andExpect(MockMvcResultMatchers.jsonPath("$.shortname").value(song.shortname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bpm").value(song.bpm))
            .andExpect(MockMvcResultMatchers.jsonPath("$.duration").value(song.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genre").value(song.genre))
            .andExpect(MockMvcResultMatchers.jsonPath("$.spotifyId").value(song.spotifyId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.album").value(song.album))
    }

    @Test
    fun `Add new song, is not successful when band exists`() {
        val errorMsg = "Duplicate"
        every { rockTunesRepository.saveSong(song) } returns Result.failure(DuplicateResourceException(errorMsg))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/rocktunes-api/song")
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.content().string(errorMsg))
    }

    @Test
    fun updateSong() {
        every { rockTunesRepository.updateSong(song.id, song) } returns Result.success(song)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/rocktunes-api/songs/{id}", song.id)
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(song.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(song.year))
            .andExpect(MockMvcResultMatchers.jsonPath("$.artist").value(song.artist))
            .andExpect(MockMvcResultMatchers.jsonPath("$.shortname").value(song.shortname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bpm").value(song.bpm))
            .andExpect(MockMvcResultMatchers.jsonPath("$.duration").value(song.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genre").value(song.genre))
            .andExpect(MockMvcResultMatchers.jsonPath("$.spotifyId").value(song.spotifyId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.album").value(song.album))
    }

    @Test
    fun `Update song by id, is not successful when song doesn't exist`() {
        val errorMsg = "Not Found"
        every {
            rockTunesRepository.updateSong(
                song.id,
                song
            )
        } returns Result.failure(ResourceNotFoundException(errorMsg))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/rocktunes-api/songs/{id}", 1)
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string(errorMsg))
    }

    @Test
    fun `Update song by id, is not successful when id on path doesn't match id on body`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/rocktunes-api/songs/{id}", 2)
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `Update song by id, fails with UnprocessableEntity exception`() {
        val errorMsg = "Unprocessable Entity"
        every {
            rockTunesRepository.updateSong(
                song.id,
                song
            )
        } returns Result.failure(UnprocessableEntityException(errorMsg))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/rocktunes-api/songs/{id}", song.id)
                .content(mapper.writeValueAsString(song))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().string(errorMsg))
    }

    @Test
    fun deleteSongById() {
        justRun { rockTunesRepository.deleteSongByIdOrNull(song.id) }

        mockMvc.perform(MockMvcRequestBuilders.delete("/rocktunes-api/song/{id}", song.id))
            .andExpect(MockMvcResultMatchers.status().isAccepted())
    }

    @Test
    fun `Delete song by id, is not successful when song does not exist`() {
        val errorMsg = "Not Found"
        every { rockTunesRepository.deleteSongByIdOrNull(song.id) } returns Result.failure(
            ResourceNotFoundException(
            errorMsg
        )
        )

        //TODO change path to songs
        mockMvc.perform(MockMvcRequestBuilders.delete("/rocktunes-api/song/{id}", song.id))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string(errorMsg))
    }

    @Test
    fun getAllSongs() {
        every { rockTunesRepository.findAllSongs() } returns listOf(song)

        mockMvc.perform(MockMvcRequestBuilders.get("/rocktunes-api/songs"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value(song.name))
    }

    @Test
    fun `Find all songs by genre`() {
        every { rockTunesRepository.findAllSongsByGenre(song.genre) } returns listOf(song)

        mockMvc.perform(MockMvcRequestBuilders.get("/rocktunes-api/songs/genre/{genre}", song.genre))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value(song.name))
    }

    @Disabled
    @Test
    fun `Find all songs by genre, returns empty list if no songs for provided genre is found`() {
        every { rockTunesRepository.findAllSongsByGenre(song.genre) } returns emptyList()

        mockMvc.perform(MockMvcRequestBuilders.get("/rocktunes-api/songs/genre/{genre}", song.genre))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty)
    }
}