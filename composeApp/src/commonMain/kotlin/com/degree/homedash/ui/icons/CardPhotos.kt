package com.degree.homedash.ui.icons

import com.degree.homedash.resources.Res
import com.degree.homedash.resources.callie
import com.degree.homedash.shared.model.entity.NavigationMetadata.CardPhoto
import org.jetbrains.compose.resources.DrawableResource

/**
 * Resolves the photo token a card declares (see [CardPhoto]) to its bundled image — the photograph
 * counterpart of [roomIcon].
 *
 * The mapping lives here rather than in `:shared` for the same reason [roomIcon]'s does: the
 * metadata carries a name, the UI owns the picture. The generated `Res` class is internal to
 * `:composeApp`, so `:shared` could not name a drawable even if it wanted to.
 */
fun cardPhoto(photo: CardPhoto): DrawableResource = when (photo) {
    CardPhoto.Callie -> Res.drawable.callie
}
