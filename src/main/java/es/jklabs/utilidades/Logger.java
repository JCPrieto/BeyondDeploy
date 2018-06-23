package es.jklabs.utilidades;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Logger {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Logger.class.getName());
    private static Logger logger;
    private static ResourceBundle errores = ResourceBundle.getBundle("i18n/errores", Locale.getDefault());

    private Logger() {
        LocalDate hoy = LocalDate.now();
        FileHandler fh;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String archivo = "log_" + hoy.format(dtf) + ".log";
        try {
            fh = new FileHandler(archivo, true);
            LOG.addHandler(fh);
            LOG.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            fh.setLevel(Level.ALL);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Crear archivo logs", e);
        }
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    public void error(String mensaje, Exception e) {
        if (errores.containsKey(mensaje)) {
            LOG.log(Level.SEVERE, errores.getString(mensaje), e);
        } else {
            LOG.log(Level.SEVERE, mensaje, e);
        }
    }

    void info(String mensaje, Exception e) {
        if (errores.containsKey(mensaje)) {
            LOG.log(Level.INFO, errores.getString(mensaje), e);
        } else {
            LOG.log(Level.INFO, mensaje, e);
        }
    }
}
