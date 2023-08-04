package nl.teamrockstars.rockstarstunes.model

data class Song(
    val id: Long,
    val name: String,
    val year: Int,
    val artist: String,
    val shortname: String,
    val bpm: Int,
    val duration: Long,
    val genre: String,
    val spotifyId: String?,
    val album: String?
)
