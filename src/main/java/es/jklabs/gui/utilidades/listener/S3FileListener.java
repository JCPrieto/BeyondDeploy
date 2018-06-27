package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.MainUI;
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
        UtilidadesS3.getObject(padre, padre.getConfiguracion().getBucketConfig(), s3File);
        //ToDo Descargar archivo
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
