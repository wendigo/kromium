package pl.wendigo.chrome.driver

import io.reactivex.Single
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

object Files {
    fun writeFile(location: String, content: ByteArray) : Single<Path> {

        return Single.defer {
            Single.just(Paths.get(location))
        }.map {
            if (it.parent != null) {
                Files.createDirectories(it.parent)
            }
            it
        }.map {
            Files.createFile(it)
        }.onErrorResumeNext {
            Single.just(Paths.get(location))
        }.map {
            Files.write(it, content, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getImageDimension(imgFile: File) : Dimension {
        val pos = imgFile.name.lastIndexOf(".")

        if (pos == -1) {
            throw IOException("No extension for file: " + imgFile.absolutePath)
        }

        val suffix = imgFile.name.substring(pos + 1)
        val iter = ImageIO.getImageReadersBySuffix(suffix)

        while (iter.hasNext()) {
            val reader = iter.next()
            try {
                val stream = FileImageInputStream(imgFile)
                reader.input = stream
                val width = reader.getWidth(reader.minIndex)
                val height = reader.getHeight(reader.minIndex)
                return Dimension(width, height)
            } catch (e: IOException) {
                throw e
            } finally {
                reader.dispose()
            }
        }

        throw IOException("Not a known image file: " + imgFile.absolutePath)
    }
}