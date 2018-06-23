package es.jklabs.gui;

import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.UtilidadesFirebase;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainUI extends JFrame {

    private static final long serialVersionUID = 7929656351162697237L;
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private static final Logger LOG = Logger.getLogger();
    private Configuracion configuracion;
    private JMenu jmArchivo;
    private JMenu jmAyuda;

    public MainUI(Configuracion configuracion) {
        this();
        this.configuracion = configuracion;
        cargarPantallaPrincipal();
        super.pack();
    }

    private void cargarPantallaPrincipal() {

    }

    private MainUI() {
        super(Constantes.NOMBRE_APP);
        super.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/database.png"))).getImage());
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        cargarMenu();
        cargarNotificaciones();
        super.pack();
    }

    private void cargarNotificaciones() {

    }

    private void cargarMenu() {
        JMenuBar menu = new JMenuBar();
        jmArchivo = new JMenu(mensajes.getString("archivo"));
        jmArchivo.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiConfiguracion = new JMenuItem(mensajes.getString("configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/settings.png"))));
        jmiConfiguracion.addActionListener(al -> abrirConfiguracion());
        JMenuItem jmiExportar = new JMenuItem(mensajes.getString("exportar.servidores"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png"))));
        jmiExportar.addActionListener(al -> exportarServidores());
        JMenuItem jmiImportar = new JMenuItem(mensajes.getString("importar.servidores"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/upload.png"))));
        jmiImportar.addActionListener(al -> importarServidores());
        jmArchivo.add(jmiConfiguracion);
        jmArchivo.add(jmiExportar);
        jmArchivo.add(jmiImportar);
        jmAyuda = new JMenu("Ayuda");
        jmAyuda.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiAcercaDe = new JMenuItem(mensajes.getString("acerca.de"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/info.png"))));
        jmiAcercaDe.addActionListener(al -> mostrarAcercaDe());
        jmAyuda.add(jmiAcercaDe);
        menu.add(jmArchivo);
        menu.add(jmAyuda);
        if (UtilidadesFirebase.existeNuevaVersion()) {
            menu.add(Box.createHorizontalGlue());
            JMenuItem jmActualizacion = new JMenuItem(mensajes.getString("existe.nueva.version"), new ImageIcon
                    (Objects.requireNonNull(getClass().getClassLoader().getResource("img/icons/update.png"))));
            jmActualizacion.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            jmActualizacion.setHorizontalTextPosition(SwingConstants.RIGHT);
            jmActualizacion.addActionListener(al -> descargarNuevaVersion());
            menu.add(jmActualizacion);
        }
        super.setJMenuBar(menu);
    }

    private void descargarNuevaVersion() {

    }

    private void mostrarAcercaDe() {

    }

    private void importarServidores() {

    }

    private void exportarServidores() {

    }

    private void abrirConfiguracion() {

    }
}
