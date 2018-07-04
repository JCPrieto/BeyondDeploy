package es.jklabs.gui.utilidades.listener;

import com.amazonaws.services.s3.model.ObjectListing;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class S3FolderListener implements MouseListener {
    private final MainUI padre;
    private final JLabel jLabel;
    private final S3Folder s3Folder;
    private final Explorador explorador;

    public S3FolderListener(MainUI padre, Explorador explorador, JLabel jLabel, S3Folder s3Folder) {
        this.padre = padre;
        this.explorador = explorador;
        this.jLabel = jLabel;
        this.s3Folder = s3Folder;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        padre.remove(explorador);
        Explorador siguiente = getExplorador();
        padre.setPanelCentral(siguiente);
        padre.add(siguiente, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(padre.getPanelCentral());
    }

    private Explorador getExplorador() {
        ObjectListing elementos = UtilidadesS3.getObjetos(padre.getConfiguracion().getBucketConfig(), s3Folder
                .getFullpath());
        UtilidadesS3.actualizarCarpeta(s3Folder, elementos);
        return new Explorador(padre, s3Folder, explorador);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        jLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        jLabel.setCursor(null);
    }
}
