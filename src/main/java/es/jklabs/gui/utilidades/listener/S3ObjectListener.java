package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3Object;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class S3ObjectListener implements MouseListener {

    private final MainUI padre;
    private final JLabel jLabel;
    private final S3Object s3Object;
    private final Explorador explorador;

    public S3ObjectListener(MainUI padre, Explorador explorador, JLabel jLabel, S3Object s3Object) {
        this.padre = padre;
        this.explorador = explorador;
        this.jLabel = jLabel;
        this.s3Object = s3Object;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
