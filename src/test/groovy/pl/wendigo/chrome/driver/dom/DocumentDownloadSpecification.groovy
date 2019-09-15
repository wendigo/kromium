package pl.wendigo.chrome.driver.dom

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import pl.wendigo.chrome.driver.BaseSpecification
import spock.lang.Ignore

import java.nio.file.Files
import java.nio.file.Paths

class DocumentDownloadSpecification extends BaseSpecification {
    @Rule
    TemporaryFolder temporaryFolder

    @Ignore
    def "should download file to given location"() {
        given:
            def frame = session.get().navigateAndWait(server.staticAddress("dom/download/index"))
            def folder = temporaryFolder.newFolder()
            def filePath = Paths.get(folder.path, "/file.bz2")

        when:
            frame.downloadFilesTo(folder.path)
            frame.query("a").click(100, true)

        then:
            Files.exists(filePath)
    }
}