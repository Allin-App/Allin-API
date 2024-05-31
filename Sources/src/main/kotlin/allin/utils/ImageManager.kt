package allin.utils

import java.io.File
import java.util.*

class ImageManager {

    fun saveImage(urlfile: String, base64Image: String): ByteArray? {
        val cleanedBase64Image = cleanBase64(base64Image)
        val imageBytes = Base64.getDecoder().decode(cleanedBase64Image)
        val file = File("${urlfile}.png")
        file.parentFile.mkdirs()
        file.writeBytes(imageBytes)
        return imageBytes
    }

    fun saveImage(urlfile: String, base64Image: ByteArray) {
        val file = File("${urlfile}.png")
        file.parentFile.mkdirs()
        file.writeBytes(base64Image)
    }

    fun imageAvailable(urlfile: String) = File(urlfile).exists()

    fun cleanBase64(base64Image: String) = base64Image.replace("\n", "").replace("\r", "")
}