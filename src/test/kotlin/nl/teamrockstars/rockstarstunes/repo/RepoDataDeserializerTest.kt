package nl.teamrockstars.rockstarstunes.repo

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class RepoDataDeserializerTest {

    @Test
    fun `deserialize unique Artists Json to Artists from a file successfully`() {
        val deserializer = RepoDataDeserializer()
        val stream = this::class.java.classLoader.getResourceAsStream("artists.json")!!
        val artists = deserializer.deserializeUniqueArtistsJson(stream)

        assertEquals(888, artists.size)
    }

    @Test
    fun `deserialize Songs Json to Songs from a file successfully`() {
        val deserializer = RepoDataDeserializer()
        val stream = this::class.java.classLoader.getResourceAsStream("songs.json")!!
        val songs = deserializer.deserializeUniqueSongsJson(stream)

        assertEquals(2517, songs.size)
    }
}