package pl.wendigo.chrome.driver.dom.input

import pl.wendigo.chrome.api.input.DispatchKeyEventRequest

object Keys {

    private const val KEY_UP = "keyUp"
    private const val KEY_DOWN = "keyDown"
    private const val KEY_CHAR = "char"

    /**
     * Encodes sequence of chars as sequence of keyboard events
     */
    fun encode(value: CharArray) : Array<DispatchKeyEventRequest> {
        val list = mutableListOf<DispatchKeyEventRequest>()

        for (char in value) {
            list += encode(char)
        }

        return list.toTypedArray()
    }

    /**
     * Encodes char as a sequence of key events
     */
    private fun encode(char: Char): Array<DispatchKeyEventRequest> {
        val key = forChar(char) ?: return encodeUnknown(char)

        val keyDown = DispatchKeyEventRequest(
                key = key.Key,
                code = key.Code,
                nativeVirtualKeyCode = key.Native,
                windowsVirtualKeyCode = key.Windows,
                type = KEY_DOWN,
                modifiers = if (key.Shift) 8 else 0
        )

        val keyUp = keyDown.copy(type = KEY_UP)

        // Printable keys need "char" event
        if (key.Print) {

            val keyChar = keyDown.copy(
                    type = KEY_CHAR,
                    text = key.Text,
                    unmodifiedText = key.Unmodified,
                    nativeVirtualKeyCode = char.toInt(),
                    windowsVirtualKeyCode = char.toInt()
            )

            return arrayOf(keyDown, keyChar, keyUp)
        }

        return arrayOf(keyDown, keyUp)
    }

    /**
     * Encodes char out of table of known chars
     */
    private fun encodeUnknown(char: Char): Array<DispatchKeyEventRequest> {
        val keyDown = DispatchKeyEventRequest(
                key = "Unidentified",
                type = KEY_DOWN
        )

        val keyUp = keyDown.copy(type= KEY_UP)

        // Printable keys need "char" event
        if (char.isUnicodePrintable()) {
            val keyChar = keyDown.copy(
                    type = KEY_CHAR,
                    text = char.toString(),
                    unmodifiedText = char.toString()
            )

            return arrayOf(keyDown, keyChar, keyUp)
        }

        return arrayOf(keyDown, keyUp)
    }

    private fun forChar(char: Char): Key? {
        return when (char) {
            '\b' -> Key(Code = "Backspace", Key = "Backspace", Text = "", Unmodified = "", Native = 8, Windows = 8, Shift = false, Print = false)
            '\t' -> Key(Code = "Tab", Key = "Tab", Text = "", Unmodified = "", Native = 9, Windows = 9, Shift = false, Print = false)
            '\r', '\n' -> Key(Code = "Enter", Key = "Enter", Text = "\r", Unmodified = "\r", Native = 13, Windows = 13, Shift = false, Print = true)
            '\u001b' -> Key(Code = "Escape", Key = "Escape", Text = "", Unmodified = "", Native = 27, Windows = 27, Shift = false, Print = false)
            ' ' -> Key(Code = "Space", Key = " ", Text = " ", Unmodified = " ", Native = 32, Windows = 32, Shift = false, Print = true)
            '!' -> Key(Code = "Digit1", Key = "!", Text = "!", Unmodified = "1", Native = 49, Windows = 49, Shift = true, Print = true)
            '"' -> Key(Code = "Quote", Key = "\"", Text = "\"", Unmodified = "'", Native = 222, Windows = 222, Shift = true, Print = true)
            '#' -> Key(Code = "Digit3", Key = "#", Text = "#", Unmodified = "3", Native = 51, Windows = 51, Shift = true, Print = true)
            '$' -> Key(Code = "Digit4", Key = "$", Text = "$", Unmodified = "4", Native = 52, Windows = 52, Shift = true, Print = true)
            '%' -> Key(Code = "Digit5", Key = "%", Text = "%", Unmodified = "5", Native = 53, Windows = 53, Shift = true, Print = true)
            '&' -> Key(Code = "Digit7", Key = "&", Text = "&", Unmodified = "7", Native = 55, Windows = 55, Shift = true, Print = true)
            '\'' -> Key(Code = "Quote", Key = "'", Text = "'", Unmodified = "'", Native = 222, Windows = 222, Shift = false, Print = true)
            '(' -> Key(Code = "Digit9", Key = "(", Text = "(", Unmodified = "9", Native = 57, Windows = 57, Shift = true, Print = true)
            ')' -> Key(Code = "Digit0", Key = ")", Text = ")", Unmodified = "0", Native = 48, Windows = 48, Shift = true, Print = true)
            '*' -> Key(Code = "Digit8", Key = "*", Text = "*", Unmodified = "8", Native = 56, Windows = 56, Shift = true, Print = true)
            '+' -> Key(Code = "Equal", Key = "+", Text = "+", Unmodified = "=", Native = 187, Windows = 187, Shift = true, Print = true)
            ',' -> Key(Code = "Comma", Key = ",", Text = ",", Unmodified = ",", Native = 188, Windows = 188, Shift = false, Print = true)
            '-' -> Key(Code = "Minus", Key = "-", Text = "-", Unmodified = "-", Native = 189, Windows = 189, Shift = false, Print = true)
            '.' -> Key(Code = "Period", Key = ".", Text = ".", Unmodified = ".", Native = 190, Windows = 190, Shift = false, Print = true)
            '/' -> Key(Code = "Slash", Key = "/", Text = "/", Unmodified = "/", Native = 191, Windows = 191, Shift = false, Print = true)
            '0' -> Key(Code = "Digit0", Key = "0", Text = "0", Unmodified = "0", Native = 48, Windows = 48, Shift = false, Print = true)
            '1' -> Key(Code = "Digit1", Key = "1", Text = "1", Unmodified = "1", Native = 49, Windows = 49, Shift = false, Print = true)
            '2' -> Key(Code = "Digit2", Key = "2", Text = "2", Unmodified = "2", Native = 50, Windows = 50, Shift = false, Print = true)
            '3' -> Key(Code = "Digit3", Key = "3", Text = "3", Unmodified = "3", Native = 51, Windows = 51, Shift = false, Print = true)
            '4' -> Key(Code = "Digit4", Key = "4", Text = "4", Unmodified = "4", Native = 52, Windows = 52, Shift = false, Print = true)
            '5' -> Key(Code = "Digit5", Key = "5", Text = "5", Unmodified = "5", Native = 53, Windows = 53, Shift = false, Print = true)
            '6' -> Key(Code = "Digit6", Key = "6", Text = "6", Unmodified = "6", Native = 54, Windows = 54, Shift = false, Print = true)
            '7' -> Key(Code = "Digit7", Key = "7", Text = "7", Unmodified = "7", Native = 55, Windows = 55, Shift = false, Print = true)
            '8' -> Key(Code = "Digit8", Key = "8", Text = "8", Unmodified = "8", Native = 56, Windows = 56, Shift = false, Print = true)
            '9' -> Key(Code = "Digit9", Key = "9", Text = "9", Unmodified = "9", Native = 57, Windows = 57, Shift = false, Print = true)
            ':' -> Key(Code = "Semicolon", Key = ":", Text = ":", Unmodified = ";", Native = 186, Windows = 186, Shift = true, Print = true)
            ';' -> Key(Code = "Semicolon", Key = ";", Text = ";", Unmodified = ";", Native = 186, Windows = 186, Shift = false, Print = true)
            '<' -> Key(Code = "Comma", Key = "<", Text = "<", Unmodified = ",", Native = 188, Windows = 188, Shift = true, Print = true)
            '=' -> Key(Code = "Equal", Key = "=", Text = "=", Unmodified = "=", Native = 187, Windows = 187, Shift = false, Print = true)
            '>' -> Key(Code = "Period", Key = ">", Text = ">", Unmodified = ".", Native = 190, Windows = 190, Shift = true, Print = true)
            '?' -> Key(Code = "Slash", Key = "?", Text = "?", Unmodified = "/", Native = 191, Windows = 191, Shift = true, Print = true)
            '@' -> Key(Code = "Digit2", Key = "@", Text = "@", Unmodified = "2", Native = 50, Windows = 50, Shift = true, Print = true)
            'A' -> Key(Code = "KeyA", Key = "A", Text = "A", Unmodified = "a", Native = 65, Windows = 65, Shift = true, Print = true)
            'B' -> Key(Code = "KeyB", Key = "B", Text = "B", Unmodified = "b", Native = 66, Windows = 66, Shift = true, Print = true)
            'C' -> Key(Code = "KeyC", Key = "C", Text = "C", Unmodified = "c", Native = 67, Windows = 67, Shift = true, Print = true)
            'D' -> Key(Code = "KeyD", Key = "D", Text = "D", Unmodified = "d", Native = 68, Windows = 68, Shift = true, Print = true)
            'E' -> Key(Code = "KeyE", Key = "E", Text = "E", Unmodified = "e", Native = 69, Windows = 69, Shift = true, Print = true)
            'F' -> Key(Code = "KeyF", Key = "F", Text = "F", Unmodified = "f", Native = 70, Windows = 70, Shift = true, Print = true)
            'G' -> Key(Code = "KeyG", Key = "G", Text = "G", Unmodified = "g", Native = 71, Windows = 71, Shift = true, Print = true)
            'H' -> Key(Code = "KeyH", Key = "H", Text = "H", Unmodified = "h", Native = 72, Windows = 72, Shift = true, Print = true)
            'I' -> Key(Code = "KeyI", Key = "I", Text = "I", Unmodified = "i", Native = 73, Windows = 73, Shift = true, Print = true)
            'J' -> Key(Code = "KeyJ", Key = "J", Text = "J", Unmodified = "j", Native = 74, Windows = 74, Shift = true, Print = true)
            'K' -> Key(Code = "KeyK", Key = "K", Text = "K", Unmodified = "k", Native = 75, Windows = 75, Shift = true, Print = true)
            'L' -> Key(Code = "KeyL", Key = "L", Text = "L", Unmodified = "l", Native = 76, Windows = 76, Shift = true, Print = true)
            'M' -> Key(Code = "KeyM", Key = "M", Text = "M", Unmodified = "m", Native = 77, Windows = 77, Shift = true, Print = true)
            'N' -> Key(Code = "KeyN", Key = "N", Text = "N", Unmodified = "n", Native = 78, Windows = 78, Shift = true, Print = true)
            'O' -> Key(Code = "KeyO", Key = "O", Text = "O", Unmodified = "o", Native = 79, Windows = 79, Shift = true, Print = true)
            'P' -> Key(Code = "KeyP", Key = "P", Text = "P", Unmodified = "p", Native = 80, Windows = 80, Shift = true, Print = true)
            'Q' -> Key(Code = "KeyQ", Key = "Q", Text = "Q", Unmodified = "q", Native = 81, Windows = 81, Shift = true, Print = true)
            'R' -> Key(Code = "KeyR", Key = "R", Text = "R", Unmodified = "r", Native = 82, Windows = 82, Shift = true, Print = true)
            'S' -> Key(Code = "KeyS", Key = "S", Text = "S", Unmodified = "s", Native = 83, Windows = 83, Shift = true, Print = true)
            'T' -> Key(Code = "KeyT", Key = "T", Text = "T", Unmodified = "t", Native = 84, Windows = 84, Shift = true, Print = true)
            'U' -> Key(Code = "KeyU", Key = "U", Text = "U", Unmodified = "u", Native = 85, Windows = 85, Shift = true, Print = true)
            'V' -> Key(Code = "KeyV", Key = "V", Text = "V", Unmodified = "v", Native = 86, Windows = 86, Shift = true, Print = true)
            'W' -> Key(Code = "KeyW", Key = "W", Text = "W", Unmodified = "w", Native = 87, Windows = 87, Shift = true, Print = true)
            'X' -> Key(Code = "KeyX", Key = "X", Text = "X", Unmodified = "x", Native = 88, Windows = 88, Shift = true, Print = true)
            'Y' -> Key(Code = "KeyY", Key = "Y", Text = "Y", Unmodified = "y", Native = 89, Windows = 89, Shift = true, Print = true)
            'Z' -> Key(Code = "KeyZ", Key = "Z", Text = "Z", Unmodified = "z", Native = 90, Windows = 90, Shift = true, Print = true)
            '[' -> Key(Code = "BracketLeft", Key = "[", Text = "[", Unmodified = "[", Native = 219, Windows = 219, Shift = false, Print = true)
            '\\' -> Key(Code = "Backslash", Key = "\\", Text = "\\", Unmodified = "\\", Native = 220, Windows = 220, Shift = false, Print = true)
            ']' -> Key(Code = "BracketRight", Key = "]", Text = "]", Unmodified = "]", Native = 221, Windows = 221, Shift = false, Print = true)
            '^' -> Key(Code = "Digit6", Key = "^", Text = "^", Unmodified = "6", Native = 54, Windows = 54, Shift = true, Print = true)
            '_' -> Key(Code = "Minus", Key = "_", Text = "_", Unmodified = "-", Native = 189, Windows = 189, Shift = true, Print = true)
            '`' -> Key(Code = "Backquote", Key = "`", Text = "`", Unmodified = "`", Native = 192, Windows = 192, Shift = false, Print = true)
            'a' -> Key(Code = "KeyA", Key = "a", Text = "a", Unmodified = "a", Native = 65, Windows = 65, Shift = false, Print = true)
            'b' -> Key(Code = "KeyB", Key = "b", Text = "b", Unmodified = "b", Native = 66, Windows = 66, Shift = false, Print = true)
            'c' -> Key(Code = "KeyC", Key = "c", Text = "c", Unmodified = "c", Native = 67, Windows = 67, Shift = false, Print = true)
            'd' -> Key(Code = "KeyD", Key = "d", Text = "d", Unmodified = "d", Native = 68, Windows = 68, Shift = false, Print = true)
            'e' -> Key(Code = "KeyE", Key = "e", Text = "e", Unmodified = "e", Native = 69, Windows = 69, Shift = false, Print = true)
            'f' -> Key(Code = "KeyF", Key = "f", Text = "f", Unmodified = "f", Native = 70, Windows = 70, Shift = false, Print = true)
            'g' -> Key(Code = "KeyG", Key = "g", Text = "g", Unmodified = "g", Native = 71, Windows = 71, Shift = false, Print = true)
            'h' -> Key(Code = "KeyH", Key = "h", Text = "h", Unmodified = "h", Native = 72, Windows = 72, Shift = false, Print = true)
            'i' -> Key(Code = "KeyI", Key = "i", Text = "i", Unmodified = "i", Native = 73, Windows = 73, Shift = false, Print = true)
            'j' -> Key(Code = "KeyJ", Key = "j", Text = "j", Unmodified = "j", Native = 74, Windows = 74, Shift = false, Print = true)
            'k' -> Key(Code = "KeyK", Key = "k", Text = "k", Unmodified = "k", Native = 75, Windows = 75, Shift = false, Print = true)
            'l' -> Key(Code = "KeyL", Key = "l", Text = "l", Unmodified = "l", Native = 76, Windows = 76, Shift = false, Print = true)
            'm' -> Key(Code = "KeyM", Key = "m", Text = "m", Unmodified = "m", Native = 77, Windows = 77, Shift = false, Print = true)
            'n' -> Key(Code = "KeyN", Key = "n", Text = "n", Unmodified = "n", Native = 78, Windows = 78, Shift = false, Print = true)
            'o' -> Key(Code = "KeyO", Key = "o", Text = "o", Unmodified = "o", Native = 79, Windows = 79, Shift = false, Print = true)
            'p' -> Key(Code = "KeyP", Key = "p", Text = "p", Unmodified = "p", Native = 80, Windows = 80, Shift = false, Print = true)
            'q' -> Key(Code = "KeyQ", Key = "q", Text = "q", Unmodified = "q", Native = 81, Windows = 81, Shift = false, Print = true)
            'r' -> Key(Code = "KeyR", Key = "r", Text = "r", Unmodified = "r", Native = 82, Windows = 82, Shift = false, Print = true)
            's' -> Key(Code = "KeyS", Key = "s", Text = "s", Unmodified = "s", Native = 83, Windows = 83, Shift = false, Print = true)
            't' -> Key(Code = "KeyT", Key = "t", Text = "t", Unmodified = "t", Native = 84, Windows = 84, Shift = false, Print = true)
            'u' -> Key(Code = "KeyU", Key = "u", Text = "u", Unmodified = "u", Native = 85, Windows = 85, Shift = false, Print = true)
            'v' -> Key(Code = "KeyV", Key = "v", Text = "v", Unmodified = "v", Native = 86, Windows = 86, Shift = false, Print = true)
            'w' -> Key(Code = "KeyW", Key = "w", Text = "w", Unmodified = "w", Native = 87, Windows = 87, Shift = false, Print = true)
            'x' -> Key(Code = "KeyX", Key = "x", Text = "x", Unmodified = "x", Native = 88, Windows = 88, Shift = false, Print = true)
            'y' -> Key(Code = "KeyY", Key = "y", Text = "y", Unmodified = "y", Native = 89, Windows = 89, Shift = false, Print = true)
            'z' -> Key(Code = "KeyZ", Key = "z", Text = "z", Unmodified = "z", Native = 90, Windows = 90, Shift = false, Print = true)
            '{' -> Key(Code = "BracketLeft", Key = "{", Text = "{", Unmodified = "[", Native = 219, Windows = 219, Shift = true, Print = true)
            '|' -> Key(Code = "Backslash", Key = "|", Text = "|", Unmodified = "\\", Native = 220, Windows = 220, Shift = true, Print = true)
            '}' -> Key(Code = "BracketRight", Key = "}", Text = "}", Unmodified = "]", Native = 221, Windows = 221, Shift = true, Print = true)
            '~' -> Key(Code = "Backquote", Key = "~", Text = "~", Unmodified = "`", Native = 192, Windows = 192, Shift = true, Print = true)
            else -> null
        }
    }
}

class Key(
    /**
     * Code is the key code ("Enter", "Comma", "KeyA" etc)
     */
    val Code: String,

    /**
     * Key is the key value ("Enter", ",", "a")
     */
    val Key: String,

    /**
     * Key is the text for printable keys
     */
    val Text: String,

    /**
     * Unmodified is unmodified text for printable keys
     */
    val Unmodified: String,

    /**
     * Native scan code
     */
    val Native: Int,

    /**
     * Windows scan code (legacy)
     */
    val Windows: Int,

    /**
     * True if shift modifier should be sent
     */
    val Shift: Boolean,

    /**
     * Is printable char (should char event be generated)
     */
    val Print: Boolean
)

internal fun Char.isUnicodePrintable() : Boolean {
    val block = Character.UnicodeBlock.of(this)
    return !Character.isISOControl(this) &&
            this.toInt() != 0xFFFF &&
            block != null &&
            block !== Character.UnicodeBlock.SPECIALS
}