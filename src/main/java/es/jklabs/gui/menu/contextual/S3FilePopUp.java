package es.jklabs.gui.menu.contextual;

import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.utilidades.Mensajes;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class S3FilePopUp extends JPopupMenu {

    private static final long serialVersionUID = 5925867780383236170L;
    private final Explorador explorador;
    private final S3File s3File;

    public S3FilePopUp(Explorador explorador, S3File s3File) {
        super();
        this.explorador = explorador;
        this.s3File = s3File;
        cargarElementos();
    }

    private void cargarElementos() {
        JMenuItem jmiDescargar = new JMenuItem(Mensajes.getMensaje("descargar"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png"))));
        jmiDescargar.addActionListener(l -> descargarArchivo());
        add(jmiDescargar);
        ImageIcon iconPapelera = getIcon("img/icons/trash.png");
        ImageIcon iconReloj = getIcon("img/icons/clock.png");
        JMenu jmVersiones = new JMenu(Mensajes.getMensaje("versiones"));
        jmVersiones.setIcon(iconReloj);
        List<S3FileVersion> s3FileVersionList = UtilidadesS3.getVersiones(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File);
        for (S3FileVersion s3FileVersion : s3FileVersionList) {
            JMenuItem jmiVersion = new JMenuItem(s3FileVersion.getFecha().toString(), iconPapelera);
            jmiVersion.addActionListener(l -> eliminarVersion(s3FileVersion));
            jmVersiones.add(jmiVersion);
        }
        add(jmVersiones);
        JMenuItem jmiEliminar = new JMenuItem(Mensajes.getMensaje("eliminar"), iconPapelera);
        jmiEliminar.addActionListener(l -> elminarArchivo());
        add(jmiEliminar);
    }

    private ImageIcon getIcon(String resource) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (resource))).getImage().getScaledInstance(24, 24, Image
                .SCALE_SMOOTH));
    }

    private void eliminarVersion(S3FileVersion s3FileVersion) {
        UtilidadesS3.elimninarVersion(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File, s3FileVersion);
        explorador.recargarPantalla();
    }

    private void elminarArchivo() {
        UtilidadesS3.deleteObject(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File);
        explorador.recargarPantalla();
    }

    private void descargarArchivo() {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File);
    }
}
