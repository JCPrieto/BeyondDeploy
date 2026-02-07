package es.jklabs.utilidades;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Logger {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Logger.class.getName());
    static final String LOG_DIR_PROPERTY = "beyonddeploy.log.dir";
    private static Logger logger;
    private static final int MAX_LOG_SIZE_BYTES = 2 * 1024 * 1024;
    private static final int MAX_LOG_FILES_PER_DAY = 5;
    private static final long MAX_LOG_AGE_MILLIS = 30L * 24 * 60 * 60 * 1000;
    private static final String DATE = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    private static final String LOG_PREFIX = "log_" + DATE;

    private Logger() {
        try {
            Path logDir = getLogDir();
            Files.createDirectories(logDir);
            String pattern = logDir.resolve(LOG_PREFIX + "_%g.log").toString();
            FileHandler fh = new FileHandler(pattern, MAX_LOG_SIZE_BYTES, MAX_LOG_FILES_PER_DAY, true);
            LOG.addHandler(fh);
            LOG.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            fh.setLevel(Level.ALL);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Crear archivo logs", e);
        }
    }

    public static void eliminarLogsVacios() {
        try {
            Path logDir = getLogDir();
            Files.createDirectories(logDir);
            long now = System.currentTimeMillis();
            List<Path> logs = new ArrayList<>();
            try (var stream = Files.list(logDir)) {
                stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".log"))
                        .forEach(logs::add);
            }
            for (Path log : logs) {
                try {
                    long size = Files.size(log);
                    if (size == 0L) {
                        Files.deleteIfExists(log);
                        continue;
                    }
                    FileTime lastModified = Files.getLastModifiedTime(log);
                    if (now - lastModified.toMillis() > MAX_LOG_AGE_MILLIS) {
                        Files.deleteIfExists(log);
                    }
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, Mensajes.getError("lectura.logs"), e);
                }
            }
            cleanupOldDailyFiles();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, Mensajes.getError("lectura.logs"), e);
        }
    }

    private static void cleanupOldDailyFiles() {
        try (var stream = Files.list(getLogDir())) {
            List<Path> todayLogs = stream
                    .filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().startsWith(LOG_PREFIX)
                            && path.getFileName().toString().endsWith(".log"))
                    .sorted(Comparator.comparingLong(Logger::lastModifiedSafe).reversed())
                    .toList();
            if (todayLogs.size() <= MAX_LOG_FILES_PER_DAY) {
                return;
            }
            for (int i = MAX_LOG_FILES_PER_DAY; i < todayLogs.size(); i++) {
                Files.deleteIfExists(todayLogs.get(i));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, Mensajes.getError("lectura.logs"), e);
        }
    }

    private static long lastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    private static Path resolveLogDir() {
        String override = System.getProperty(LOG_DIR_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty()) {
                return Paths.get(localAppData, "BeyondDeploy", "logs");
            }
            return Paths.get(home, "AppData", "Local", "BeyondDeploy", "logs");
        }
        if (os.contains("mac")) {
            return Paths.get(home, "Library", "Application Support", "BeyondDeploy", "logs");
        }
        return Paths.get(home, ".local", "share", "BeyondDeploy", "logs");
    }

    private static Path getLogDir() {
        return resolveLogDir();
    }

    public static void init() {
        if (logger == null) {
            logger = new Logger();
        }
    }

    public static void error(String mensaje, Exception e) {
        LOG.log(Level.SEVERE, Mensajes.getError(mensaje), e);
    }

    static void info(String mensaje, Exception e) {
        LOG.log(Level.INFO, Mensajes.getError(mensaje), e);
    }

    static void info(String mensaje) {
        LOG.log(Level.INFO, mensaje);
    }

    public static void error(Exception e) {
        LOG.log(Level.SEVERE, null, e);
    }
}
