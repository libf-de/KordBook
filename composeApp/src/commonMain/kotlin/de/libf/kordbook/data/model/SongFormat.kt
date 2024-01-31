package de.libf.kordbook.data.model

import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.NullViewer
import de.libf.kordbook.ui.components.SongViewerInterface
import de.libf.kordbook.ui.components.UltimateGuitarViewer

enum class SongFormat(val viewer: SongViewerInterface) {
    CHORDPRO(ChordProViewer),
    UG(UltimateGuitarViewer),
    NULL(NullViewer)
}