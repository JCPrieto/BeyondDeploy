package es.jklabs.gui;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.dialogos.AcercaDe;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.filter.JSonFilter;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.*;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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
    private TrayIcon trayIcon;
    private JPanel panelCentral;

    public MainUI(Configuracion configuracion) {
        this();
        this.configuracion = configuracion;
        cargarPantallaPrincipal();
        super.pack();
    }

    private void cargarPantallaPrincipal() {
        super.setLayout(new BorderLayout(10, 10));
        ObjectListing elementos = UtilidadesS3.getRaiz(configuracion.getBucketConfig());
        S3Folder raiz = new S3Folder();
        for (S3ObjectSummary s3ObjectSummary : elementos.getObjectSummaries()) {
            if (s3ObjectSummary.getKey().endsWith("/")) {
                String[] ruta = s3ObjectSummary.getKey().split("/");
                S3Folder actual = raiz;
                for (String carpeta : ruta) {
                    actual = addCarpetas(actual, carpeta);
                }
            } else {
                String[] ruta = s3ObjectSummary.getKey().split("/");
                S3Folder actual = raiz;
                for (int i = 0; i < ruta.length - 1; i++) {
                    String carpeta = ruta[i];
                    actual = addCarpetas(actual, carpeta);
                }
                actual.getS3Files().add(new S3File(ruta[ruta.length - 1], s3ObjectSummary));
            }
        }
        panelCentral = new Explorador(this, raiz);
        super.add(panelCentral, BorderLayout.CENTER);
    }

    private S3Folder addCarpetas(S3Folder actual, String carpeta) {
        boolean existeCarpeta = false;
        for (S3Folder s3Folder : actual.getS3Forlders()) {
            if (Objects.equals(s3Folder.getName(), carpeta)) {
                existeCarpeta = true;
                actual = s3Folder;
                break;
            }
        }
        if (!existeCarpeta) {
            S3Folder nueva = new S3Folder(carpeta);
            actual.getS3Forlders().add(nueva);
            actual = nueva;
        }
        return actual;
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
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new JSonFilter());
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retorno = fc.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            configuracion = UtilidadesConfiguracion.loadConfig(file);
        }
    }

    private void exportarConfiguracion() {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new JSonFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int retorno = fc.showSaveDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!Objects.equals(FilenameUtils.getExtension(file.getName()), "json")) {
                file = new File(file.toString() + ".json");
            }
            UtilidadesConfiguracion.guardarConfiguracion(configuracion, file);
        }
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

    public JPanel getPanelCentral() {
        return panelCentral;
    }

    public void setPanelCentral(Explorador panelCentral) {
        this.panelCentral = panelCentral;
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(Configuracion configuracion) {
        this.configuracion = configuracion;
    }
}
