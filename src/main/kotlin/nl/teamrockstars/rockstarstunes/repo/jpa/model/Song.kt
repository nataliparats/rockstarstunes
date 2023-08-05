package nl.teamrockstars.rockstarstunes.repo.jpa.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Song(
   @Id val id: Long,
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
