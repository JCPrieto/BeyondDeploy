package es.jklabs.gui.utilidades.filter;

import org.apache.commons.io.FilenameUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class JSonFilter extends FileFilter {

    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || Objects.equals(FilenameUtils.getExtension(f.getName()), "json");
    }

    @Override
    public String getDescription() {
        return mensajes.getString("file.chooser.json");
    }
}
