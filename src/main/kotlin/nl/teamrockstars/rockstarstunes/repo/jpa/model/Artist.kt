package nl.teamrockstars.rockstarstunes.repo.jpa.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Artist(
    @Id val id: Long,
    val name: String
)