package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3Folder;

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
        if (SwingUtilities.isLeftMouseButton(e)) {
            padre.remove(explorador);
            Explorador siguiente = new Explorador(padre, s3Folder, explorador);
            padre.setPanelCentral(siguiente);
            padre.add(siguiente, BorderLayout.CENTER);
            SwingUtilities.updateComponentTreeUI(padre.getPanelCentral());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //
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
