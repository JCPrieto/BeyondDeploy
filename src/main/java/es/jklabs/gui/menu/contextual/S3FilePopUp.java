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
    private static final String ELIMINAR = "eliminar";
    private static final String IMG_ICONS_TRASH_PNG = "img/icons/trash.png";
    private final Explorador explorador;
    private final S3File s3File;

    public S3FilePopUp(Explorador explorador, S3File s3File) {
        super();
        this.explorador = explorador;
        this.s3File = s3File;
        cargarElementos();
    }

    private void cargarElementos() {
        ImageIcon iconDownload = new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png")));
        JMenuItem jmiDescargar = new JMenuItem(Mensajes.getMensaje("descargar"), iconDownload);
        jmiDescargar.addActionListener(l -> descargarArchivo());
        add(jmiDescargar);
        ImageIcon iconPapelera = getIcon(IMG_ICONS_TRASH_PNG);
        ImageIcon iconReloj = getIcon("img/icons/clock.png");
        JMenu jmVersiones = new JMenu(Mensajes.getMensaje("versiones"));
        jmVersiones.setIcon(iconReloj);
        List<S3FileVersion> s3FileVersionList = UtilidadesS3.getVersiones(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File);
        for (S3FileVersion s3FileVersion : s3FileVersionList) {
            JMenu jmRegVersion = new JMenu(s3FileVersion.getFecha().toString());
            JMenuItem jmiDownloadVersion = new JMenuItem(Mensajes.getMensaje("descargar"), iconDownload);
            JMenuItem jmiDeleteVersion = new JMenuItem(Mensajes.getMensaje(ELIMINAR), iconPapelera);
            jmiDownloadVersion.addActionListener(l -> descargarVersion(s3FileVersion));
            jmiDeleteVersion.addActionListener(l -> eliminarVersion(s3FileVersion));
            jmRegVersion.add(jmiDownloadVersion);
            jmRegVersion.add(jmiDeleteVersion);
            jmVersiones.add(jmRegVersion);
        }
        add(jmVersiones);
        JMenuItem jmiEliminar = new JMenuItem(Mensajes.getMensaje(ELIMINAR), iconPapelera);
        jmiEliminar.addActionListener(l -> elminarArchivo());
        add(jmiEliminar);
    }

    private void descargarVersion(S3FileVersion s3FileVersion) {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3FileVersion);
    }

    private ImageIcon getIcon(String resource) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (resource))).getImage().getScaledInstance(24, 24, Image
                .SCALE_SMOOTH));
    }

    private void eliminarVersion(S3FileVersion s3FileVersion) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (IMG_ICONS_TRASH_PNG)));
        int input = JOptionPane.showConfirmDialog(explorador, Mensajes.getMensaje("confirmacion.eliminar", new String[]{s3FileVersion.getFecha().toString()}), Mensajes.getMensaje(ELIMINAR),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (input == JOptionPane.YES_OPTION) {
            UtilidadesS3.elimninarVersion(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File, s3FileVersion);
            explorador.recargarPantalla();
        }
    }

    private void elminarArchivo() {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (IMG_ICONS_TRASH_PNG)));
        int input = JOptionPane.showConfirmDialog(explorador, Mensajes.getMensaje("confirmacion.eliminar", new String[]{s3File.getName()}), Mensajes.getMensaje(ELIMINAR),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (input == JOptionPane.YES_OPTION) {
            UtilidadesS3.deleteObject(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File);
            explorador.recargarPantalla();
        }
    }

    private void descargarArchivo() {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File);
    }
}
