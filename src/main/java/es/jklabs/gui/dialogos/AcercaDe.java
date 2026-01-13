package es.jklabs.gui.dialogos;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.listener.UrlMouseListener;
import es.jklabs.utilidades.Constantes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class AcercaDe extends JDialog {

    private static final long serialVersionUID = -3603153746836186743L;
    private static final ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());

    public AcercaDe(MainUI mainUI) {
        super(mainUI, mensajes.getString("acerca.de"), true);
        cargarPantalla();
    }

    private static JLabel getEmailLabel() {
        JLabel jLabelMyMail = new JLabel("JuanC.Prieto.Silos@gmail.com", SwingConstants.LEFT);
        jLabelMyMail.setAlignmentX(CENTER_ALIGNMENT);
        jLabelMyMail.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(
                            "mailto:JuanC.Prieto.Silos@gmail.com?subject=BeyondDeploy"));
                } catch (IOException | URISyntaxException e1) {
                    Growls.mostrarError("acerca.de", "app.envio.correo", e1);
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
                jLabelMyMail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                jLabelMyMail.setCursor(null);
            }
        });
        return jLabelMyMail;
    }

    private void cargarPantalla() {
        final JPanel panel = new JPanel();
        int yPosition = 0;
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        final GridBagConstraints cns = new GridBagConstraints();
        final JLabel jLabelTitle = new JLabel(
                "<html><h1>" + Constantes.NOMBRE_APP + " " + Constantes.VERSION + "</h1></html>", new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/s3-bucket.png"))), SwingConstants.CENTER);
        cns.fill = GridBagConstraints.HORIZONTAL;
        cns.insets = new Insets(10, 10, 10, 10);
        cns.gridx = 0;
        cns.gridy = yPosition++;
        cns.gridwidth = 3;
        panel.add(jLabelTitle, cns);
        final JLabel jLabelCreadoPor = new JLabel(mensajes.getString("creado.por"), SwingConstants.LEFT);
        cns.insets = new Insets(10, 10, 3, 10);
        cns.gridy = yPosition++;
        cns.gridwidth = 1;
        panel.add(jLabelCreadoPor, cns);
        final JLabel jLabelMyName = new JLabel("<html><b>Juan Carlos Prieto Silos</b></html>", SwingConstants.LEFT);
        cns.insets = new Insets(3, 10, 3, 10);
        cns.gridy = yPosition++;
        panel.add(jLabelMyName, cns);
        final JLabel jLabelMyWeb = new JLabel("Web", SwingConstants.LEFT);
        jLabelMyWeb.addMouseListener(new UrlMouseListener(jLabelMyWeb, "https://www.jcprieto.es"));
        cns.gridx = 1;
        panel.add(jLabelMyWeb, cns);
        JLabel jLabelMyMail = getEmailLabel();
        cns.gridx = 2;
        panel.add(jLabelMyMail, cns);
        final JLabel jLabelPoweredBy = new JLabel(mensajes.getString("powered.by"), SwingConstants.LEFT);
        cns.insets = new Insets(10, 10, 3, 10);
        cns.gridx = 0;
        yPosition++;
        cns.gridy = yPosition++;
        panel.add(jLabelPoweredBy, cns);
        addPowered(panel, cns, yPosition++, "Papirus", "https://github.com/PapirusDevelopmentTeam/papirus-icon-theme");
        addPowered(panel, cns, yPosition++, "Jackson", "https://github.com/FasterXML/jackson-core/wiki");
        addPowered(panel, cns, yPosition++, "GitHub Releases", "https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases");
        addPowered(panel, cns, yPosition++, "AWS Amazon S3", "https://aws.amazon.com/sdkforjava");
        addPowered(panel, cns, yPosition++, "Apache Commons Lang", "https://commons.apache.org/proper/commons-lang");
        addPowered(panel, cns, yPosition++, "Apache Commons IO", "https://commons.apache.org/proper/commons-io");
        JLabel jLabelLicense = new JLabel
                ("<html><i>Esta obra está bajo una licencia de Creative Commons " +
                        "Reconocimiento-NoComercial-CompartirIgual 4.0 Internacional</i><html>", new ImageIcon(Objects
                        .requireNonNull(getClass().getClassLoader().getResource
                                ("img/icons/creative_commons.png"))), SwingConstants.TRAILING);
        jLabelLicense.addMouseListener(new UrlMouseListener(jLabelLicense, "https://creativecommons.org/licenses/by-nc-sa/4.0/"));
        cns.insets = new Insets(10, 10, 10, 10);
        cns.gridx = 0;
        cns.gridy = yPosition++;
        cns.gridwidth = 3;
        panel.add(jLabelLicense, cns);
        JButton botonOk = new JButton(mensajes.getString("aceptar"));
        botonOk.addActionListener(al -> pressAceptar());
        cns.gridy = yPosition;
        panel.add(botonOk, cns);
        super.add(panel);
        super.pack();
    }

    private void pressAceptar() {
        this.dispose();
    }

    private void addPowered(JPanel panel, GridBagConstraints cns, int y, String titulo, String url) {
        JLabel jLabelTitulo = new JLabel("<html><b>" + titulo + "</b></html>", SwingConstants.LEFT);
        if (url != null) {
            jLabelTitulo.addMouseListener(new UrlMouseListener(jLabelTitulo, url));
        }
        cns.insets = new Insets(3, 10, 3, 10);
        cns.gridx = 0;
        cns.gridy = y;
        cns.gridwidth = 1;
        panel.add(jLabelTitulo, cns);
        if (url != null) {
            JLabel jLabelUrl = new JLabel(url, SwingConstants.LEFT);
            jLabelUrl.addMouseListener(new UrlMouseListener(jLabelUrl, url));
            cns.gridx = 1;
            cns.gridy = y;
            cns.gridwidth = 2;
            panel.add(jLabelUrl, cns);
        }
    }
}
