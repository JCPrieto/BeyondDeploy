package es.jklabs.gui.dialogos;

import es.jklabs.gui.utilidades.listener.UrlMouseListener;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AcercaDeTest {

    private static boolean tieneUrlMouseListener(JLabel label) {
        return java.util.Arrays.stream(label.getMouseListeners())
                .anyMatch(UrlMouseListener.class::isInstance);
    }

    @Test
    public void addPoweredConUrlAnadeTituloYEnlace() {
        JPanel panel = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();

        AcercaDe.addPowered(panel, constraints, 1, "Proyecto", "https://example.com");

        assertEquals(2, panel.getComponentCount());
        JLabel titulo = (JLabel) panel.getComponent(0);
        JLabel url = (JLabel) panel.getComponent(1);
        assertEquals("<html><b>Proyecto</b></html>", titulo.getText());
        assertEquals("https://example.com", url.getText());
        assertTrue(tieneUrlMouseListener(titulo));
        assertTrue(tieneUrlMouseListener(url));
    }

    @Test
    public void addPoweredSinUrlAnadeSoloTitulo() {
        JPanel panel = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();

        AcercaDe.addPowered(panel, constraints, 1, "Proyecto", null);

        assertEquals(1, panel.getComponentCount());
        JLabel titulo = (JLabel) panel.getComponent(0);
        assertEquals("<html><b>Proyecto</b></html>", titulo.getText());
        assertEquals(0, titulo.getMouseListeners().length);
    }
}
