package pl.wendigo.chrome.driver.dom

import pl.wendigo.chrome.api.dom.BoxModel

class Position (
    val left: Int,
    val top: Int
) {
    companion object {
        fun fromBoxModel(model: BoxModel) : Position {
            var x = 0
            var y = 0

            for (i in 0 until model.content.size step 2) {
                x += model.content[i].toInt()
                y += model.content[i + 1].toInt()
            }

            return Position((x / (model.content.size / 2)), (y / (model.content.size / 2)))
        }
    }
}