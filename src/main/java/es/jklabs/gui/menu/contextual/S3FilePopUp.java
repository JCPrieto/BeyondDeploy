package es.jklabs.gui.menu.contextual;

import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3File;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class S3FilePopUp extends JPopupMenu {

    private static final long serialVersionUID = 5925867780383236170L;
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private final Explorador explorador;
    private final S3File s3File;

    public S3FilePopUp(Explorador explorador, S3File s3File) {
        super();
        this.explorador = explorador;
        this.s3File = s3File;
        cargarElementos();
    }

    private void cargarElementos() {
        JMenuItem jmiDescargar = new JMenuItem(mensajes.getString("descargar"));
        jmiDescargar.addActionListener(l -> descargarArchivo());
        JMenuItem jmiEliminar = new JMenuItem(mensajes.getString("eliminar"));
        jmiEliminar.addActionListener(l -> elminarArchivo());
        add(jmiDescargar);
        add(jmiEliminar);
    }

    private void elminarArchivo() {
        UtilidadesS3.deleteObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File);
        explorador.recargarPantalla();
    }

    private void descargarArchivo() {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File);
    }
}
