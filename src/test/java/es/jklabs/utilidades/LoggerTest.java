package es.jklabs.utilidades;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoggerTest {

    private Path logDir;

    @Before
    public void setup() throws Exception {
        logDir = Files.createTempDirectory("beyonddeploy-logs-test");
        System.setProperty(Logger.LOG_DIR_PROPERTY, logDir.toString());
    }

    @After
    public void cleanup() {
        System.clearProperty(Logger.LOG_DIR_PROPERTY);
    }

    @Test
    public void eliminarLogsVaciosBorraFicherosVacios() throws Exception {
        Path emptyLog = logDir.resolve("empty.log");
        Files.createFile(emptyLog);

        Logger.eliminarLogsVacios();

        assertFalse(Files.exists(emptyLog));
    }

    @Test
    public void eliminarLogsVaciosMantieneLogRecienteConContenido() throws Exception {
        Path nonEmptyLog = logDir.resolve("recent.log");
        Files.writeString(nonEmptyLog, "contenido");

        Logger.eliminarLogsVacios();

        assertTrue(Files.exists(nonEmptyLog));
    }

    @Test
    public void eliminarLogsVaciosEliminaLogsAntiguos() throws Exception {
        Path oldLog = logDir.resolve("old.log");
        Files.writeString(oldLog, "contenido");
        FileTime oldTime = FileTime.from(Instant.now().minusSeconds(31L * 24L * 60L * 60L));
        Files.setLastModifiedTime(oldLog, oldTime);

        Logger.eliminarLogsVacios();

        assertFalse(Files.exists(oldLog));
    }
}
