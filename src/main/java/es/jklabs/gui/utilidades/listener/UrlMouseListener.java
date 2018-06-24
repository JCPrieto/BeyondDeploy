package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UrlMouseListener implements MouseListener {

    private final MainUI padre;
    private final JLabel etiqueta;
    private final String url;

    public UrlMouseListener(MainUI padre, JLabel etiqueta, String url) {
        this.padre = padre;
        this.etiqueta = etiqueta;
        this.url = url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e1) {
            Growls.mostrarError(padre, "abrir.enlace", e1);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        etiqueta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        etiqueta.setCursor(null);
    }
}
