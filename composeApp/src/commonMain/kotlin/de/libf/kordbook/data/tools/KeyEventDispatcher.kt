package de.libf.kordbook.data.tools

import androidx.compose.ui.input.key.KeyEvent

class KeyEventDispatcher {
    private var lastKeyEvent: KeyEvent? = null

    val onKeyChange: (KeyEvent?) -> Unit = { keyEvent ->
        lastKeyEvent = keyEvent
        // Führen Sie hier die gewünschte Logik aus
    }

    fun handleKeyEvent(keyEvent: KeyEvent) {
        onKeyChange(keyEvent)
    }

    fun getLastKeyPressed(): KeyEvent? = lastKeyEvent
}