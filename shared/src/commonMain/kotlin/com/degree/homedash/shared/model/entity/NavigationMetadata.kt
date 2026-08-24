package com.degree.homedash.shared.model.entity

/**
 * A launcher card that opens another dashboard.
 *
 * Unlike every other [DeviceMetadata] there is no Home Assistant entity behind this one: it carries no
 * live state, and [entityId] is synthetic — derived from [destination] purely to give the card a stable
 * list key.
 */
data class NavigationMetadata(
    val destination: NavigationTarget,
    override val displayName: String,
    val icon: RoomIcon,
    val tint: Long,
    /**
     * Non-null when the card draws a photograph in place of its [icon] — the launcher's Pets tile
     * shows the cat rather than a paw print. [icon] stays required either way: a card's dashboard
     * header still draws the glyph (see `PetsScreen`), and it is the fallback if a photo can't load.
     */
    val photo: CardPhoto? = null,
) : DeviceMetadata {

    override val entityId: String get() = "nav.${destination.name.lowercase()}"

    /** Where the card goes. The UI maps this to a navigation destination and a tint. */
    enum class NavigationTarget {
        Office,
        Plants,
        LivingRoom,
        Bedroom,
        Pets
    }

    /**
     * Which glyph a card draws, named for the drawing rather than the room so a dashboard's icon and
     * its destination can differ. A token, not a picture: `:shared` has no Compose dependency, so the
     * UI resolves these to `ImageVector`s (see `RoomIcons`).
     */
    enum class RoomIcon {
        Desk,
        Plant,
        Sofa,
        Bed,
        Paw
    }

    /**
     * Which photograph a card draws, named for its subject the way [RoomIcon] is named for its
     * drawing. A token, not a picture: `:shared` has no Compose dependency, so the UI resolves these
     * to bundled image resources (see `CardPhotos`).
     */
    enum class CardPhoto {
        Callie
    }
}
