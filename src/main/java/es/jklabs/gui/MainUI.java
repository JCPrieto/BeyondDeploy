package es.jklabs.gui;

import com.amazonaws.services.s3.model.ObjectListing;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.dialogos.AcercaDe;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3Object;
import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.UtilidadesFirebase;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
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
    private TrayIcon trayIcon;

    public MainUI(Configuracion configuracion) {
        this();
        this.configuracion = configuracion;
        cargarPantallaPrincipal();
        super.pack();
    }

    private void cargarPantallaPrincipal() {
        super.setLayout(new BorderLayout(10, 10));
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 10, 10));
        panelCentral.setBorder(new EmptyBorder(10, 0, 0, 0));
        ObjectListing elementos = UtilidadesS3.getRaiz();
        super.add(panelCentral, BorderLayout.CENTER);
    }

    private MainUI() {
        super(Constantes.NOMBRE_APP);
        super.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/s3-bucket.png"))).getImage());
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        cargarMenu();
        cargarNotificaciones();
        super.pack();
    }

    private void cargarNotificaciones() {
        SystemTray tray = SystemTray.getSystemTray();
        //Alternative (if the icon is on the classpath):
        trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/s3-bucket.png"))).getImage(), Constantes.NOMBRE_APP);
        //Let the system resizes the image if needed
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            LOG.error("establecer.icono.systray", e);
        }
    }

    private void cargarMenu() {
        JMenuBar menu = new JMenuBar();
        jmArchivo = new JMenu(mensajes.getString("archivo"));
        jmArchivo.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiConfiguracion = new JMenuItem(mensajes.getString("configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/settings.png"))));
        jmiConfiguracion.addActionListener(al -> abrirConfiguracion());
        JMenuItem jmiExportar = new JMenuItem(mensajes.getString("exportar.configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png"))));
        jmiExportar.addActionListener(al -> exportarConfiguracion());
        JMenuItem jmiImportar = new JMenuItem(mensajes.getString("importar.configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/upload.png"))));
        jmiImportar.addActionListener(al -> importarConfiguracion());
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
        try {
            if (UtilidadesFirebase.existeNuevaVersion()) {
                menu.add(Box.createHorizontalGlue());
                JMenuItem jmActualizacion = new JMenuItem(mensajes.getString("existe.nueva.version"), new ImageIcon
                        (Objects.requireNonNull(getClass().getClassLoader().getResource("img/icons/update.png"))));
                jmActualizacion.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                jmActualizacion.setHorizontalTextPosition(SwingConstants.RIGHT);
                jmActualizacion.addActionListener(al -> descargarNuevaVersion());
                menu.add(jmActualizacion);
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("consultar.nueva.version", e);
        }
        super.setJMenuBar(menu);
    }

    private void descargarNuevaVersion() {
        try {
            UtilidadesFirebase.descargaNuevaVersion(this);
        } catch (InterruptedException e) {
            Growls.mostrarError(this, "descargar.nueva.version", e);
            Thread.currentThread().interrupt();
        }
    }

    private void mostrarAcercaDe() {
        AcercaDe acercaDe = new AcercaDe(this);
        acercaDe.setVisible(true);
    }

    private void importarConfiguracion() {

    }

    private void exportarConfiguracion() {

    }

    private void abrirConfiguracion() {
        ConfiguracionUI configuracionUI = new ConfiguracionUI(this, configuracion);
        configuracionUI.setVisible(true);
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }
}
