package pl.wendigo.chrome.driver.dom

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import pl.wendigo.chrome.driver.BaseSpecification

import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Paths

class DocumentDumpingSpecification extends BaseSpecification {
    @Rule
    TemporaryFolder temporaryFolder

    def "should write screenshot to location"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("demo"))
            def outputFolder = temporaryFolder.newFolder()

        when:
            document.screenshot(outputFolder.path + "/screen.png", "png", 100)
            def filePath = Paths.get(outputFolder.path, "/screen.png")

        then:
            Files.exists(filePath)

            def image = ImageIO.read(filePath.toFile())

            with (image) {
                width == 1024
                height == 768
            }
    }

    def "should write fullpage screenshot to location"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("dom/info/high"))
            def outputFolder = temporaryFolder.newFolder()

        when:
            document.fullpageScreenshot(outputFolder.path + "/fullpagescreen.png", "png", 100)
            def filePath = Paths.get(outputFolder.path, "/fullpagescreen.png")

        then:
            Files.exists(filePath)

            def image = ImageIO.read(filePath.toFile())

            with (image) {
                width == 2008
                height == 3016
            }
    }

    def "should write pdf to location"() {
        given:
            def document = session.get().navigateAndWait(server.staticAddress("demo"))
            def outputFolder = temporaryFolder.newFolder()

        when:
            document.pdf(outputFolder.path + "/page.pdf")

        then:
            Files.exists(Paths.get(outputFolder.path, "/page.pdf"))
    }
}
