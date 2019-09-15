package pl.wendigo.chrome.driver.dom

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import pl.wendigo.chrome.driver.BaseSpecification

import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Paths

class NodeDumpingSpecification extends BaseSpecification {
    @Rule
    TemporaryFolder temporaryFolder

    def "should dump and crop node screenshot"() {
        given:
            def session = session.get()
            def outputFolder = temporaryFolder.newFolder()

        when:
            session
                .navigateAndWait(server.staticAddress("dom/info/screen"))
                .query("#box")
                .screenshot(outputFolder.path + "/node.png", "png", 100)

            def filePath = Paths.get(outputFolder.path, "/node.png")

        then:
            Files.exists(filePath)

            def dimensions = pl.wendigo.chrome.driver.Files.getImageDimension(filePath.toFile())

            with (dimensions) {
                width == 190
                height == 190
            }
    }
}
