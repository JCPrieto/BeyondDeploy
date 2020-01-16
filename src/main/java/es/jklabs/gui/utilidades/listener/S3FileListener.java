package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.menu.contextual.S3FilePopUp;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3File;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class S3FileListener implements MouseListener {

    private final MainUI padre;
    private final JLabel jLabel;
    private final S3File s3File;
    private final Explorador explorador;

    public S3FileListener(MainUI padre, Explorador explorador, JLabel jLabel, S3File s3File) {
        this.padre = padre;
        this.explorador = explorador;
        this.jLabel = jLabel;
        this.s3File = s3File;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            UtilidadesS3.getObject(padre, padre.getConfiguracion().getBucketConfig(), s3File);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            S3FilePopUp s3FilePopUp = new S3FilePopUp(explorador, s3File);
            s3FilePopUp.show(e.getComponent(), e.getX(), e.getY());
        }
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
