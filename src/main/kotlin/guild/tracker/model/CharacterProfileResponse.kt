package guild.tracker.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CharacterProfileResponse(
    val id: Long,
    val name: String,
    val gender: Gender,
    val faction: Faction,
    val race: Race,
    @JsonProperty("character_class")
    val characterClass: CharacterClass,
    @JsonProperty("active_spec")
    val activeSpec: ActiveSpec,
    val realm: Realm,
    val guild: Guild,
    val level: Int,
    val experience: Long,
    @JsonProperty("last_login_timestamp")
    val lastLoginTimestamp: Long,
    @JsonProperty("average_item_level")
    val averageItemLevel: Int,
    @JsonProperty("equipped_item_level")
    val equippedItemLevel: Int,
    @JsonProperty("name_search")
    val nameSearch: String
)

@Serdeable
data class Gender(
    val type: GenderType,
    val name: String
)

@Serdeable
enum class GenderType {
    MALE,
    FEMALE,
    UNKNOWN
}

@Serdeable
@Introspected
data class Faction(
    val type: FactionType,
    val name: String
)

@Serdeable
enum class FactionType {
    HORDE,
    ALLIANCE,
    NEUTRAL
}

@Serdeable
data class Race(
    val key: Key,
    val name: String,
    val id: Int
)

@Serdeable
data class Key(
    val href: String
)

@Serdeable
data class CharacterClass(
    val key: Key,
    val name: String,
    val id: Int
)

@Serdeable
data class ActiveSpec(
    val key: Key,
    val name: String,
    val id: Int
)

@Serdeable
data class Realm(
    val key: Key,
    val name: String,
    val id: Int,
    val slug: String
)

@Serdeable
data class Guild(
    val key: Key,
    val name: String,
    val id: Long,
    val realm: Realm,
    val faction: Faction
)
