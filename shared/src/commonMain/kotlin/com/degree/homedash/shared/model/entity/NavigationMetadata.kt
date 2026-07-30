package com.degree.homedash.shared.model.entity

/**
 * A launcher card that opens another dashboard.
 *
 * Unlike every other [EntityMetadata] there is no Home Assistant entity behind this one: it carries no
 * live state, and [entityId] is synthetic — derived from [destination] purely to give the card a stable
 * list key.
 */
data class NavigationMetadata(
    val destination: NavigationTarget,
    override val displayName: String,
    val icon: RoomIcon,
    val tint: RoomTint,
) : EntityMetadata {

    override val entityId: String get() = "nav.${destination.name.lowercase()}"

    /** Where the card goes. The UI maps this to a navigation destination and a tint. */
    enum class NavigationTarget {
        Office,
        Plants,
        LivingRoom,
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
        Paw
    }

    /**
     * What colour a card's glyph is tinted. Like [RoomIcon] these are tokens, not values — `:shared`
     * has no Compose dependency and so can't hold a `Color`. The names mirror the entries in the UI's
     * `AppColors` palette that resolve them, keeping that mapping one-to-one.
     */
    enum class RoomTint {
        Wet,
        Healthy,
        Accent
    }
}
