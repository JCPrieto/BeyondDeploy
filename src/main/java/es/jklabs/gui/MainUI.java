package es.jklabs.gui;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.dialogos.AcercaDe;
import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.filter.JSonFilter;
import es.jklabs.json.configuracion.Configuracion;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.*;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainUI extends JFrame {

    private static final long serialVersionUID = 7929656351162697237L;
    private Configuracion configuracion;
    private JPanel panelCentral;
    private S3Folder raiz;

    public MainUI(Configuracion configuracion) {
        this();
        this.configuracion = configuracion;
        cargarPantallaPrincipal();
        super.pack();
    }

    private MainUI() {
        super(Constantes.NOMBRE_APP);
        super.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/s3-bucket.png"))).getImage());
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(500, 500));
        cargarMenu();
    }

    private void cargarPanelCentral() {
        if (configuracion.getBucketConfig() != null) {
            pintarElementosBucket();
        } else {
            panelCentral = new JPanel();
        }
    }

    private void pintarElementosBucket() {
        try {
            raiz = new S3Folder();
            panelCentral = new Explorador(this, raiz);
        } catch (AmazonS3Exception e) {
            Growls.mostrarError("configura.bucket.incorrecta", e);
            panelCentral = new JPanel();
        }
    }

    private void cargarPantallaPrincipal() {
        super.setLayout(new BorderLayout());
        cargarPanelCentral();
        super.add(panelCentral, BorderLayout.CENTER);
    }

    private void cargarMenu() {
        JMenuBar menu = new JMenuBar();
        JMenu jmArchivo = new JMenu(Mensajes.getMensaje("archivo"));
        jmArchivo.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiConfiguracion = new JMenuItem(Mensajes.getMensaje("configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/settings.png"))));
        jmiConfiguracion.addActionListener(al -> abrirConfiguracion());
        JMenuItem jmiExportar = new JMenuItem(Mensajes.getMensaje("exportar.configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png"))));
        jmiExportar.addActionListener(al -> exportarConfiguracion());
        JMenuItem jmiImportar = new JMenuItem(Mensajes.getMensaje("importar.configuracion"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/upload.png"))));
        jmiImportar.addActionListener(al -> importarConfiguracion());
        jmArchivo.add(jmiConfiguracion);
        jmArchivo.add(jmiExportar);
        jmArchivo.add(jmiImportar);
        JMenu jmAyuda = new JMenu("Ayuda");
        jmAyuda.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiAcercaDe = new JMenuItem(Mensajes.getMensaje("acerca.de"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/info.png"))));
        jmiAcercaDe.addActionListener(al -> mostrarAcercaDe());
        jmAyuda.add(jmiAcercaDe);
        menu.add(jmArchivo);
        menu.add(jmAyuda);
        try {
            if (UtilidadesGithubRelease.existeNuevaVersion()) {
                menu.add(Box.createHorizontalGlue());
                JMenuItem jmActualizacion = new JMenuItem(Mensajes.getMensaje("existe.nueva.version"), new ImageIcon
                        (Objects.requireNonNull(getClass().getClassLoader().getResource("img/icons/update.png"))));
                jmActualizacion.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                jmActualizacion.setHorizontalTextPosition(SwingConstants.RIGHT);
                jmActualizacion.addActionListener(al -> descargarNuevaVersion());
                menu.add(jmActualizacion);
            }
        } catch (IOException e) {
            Logger.error("consultar.nueva.version", e);
        }
        super.setJMenuBar(menu);
    }

    private void descargarNuevaVersion() {
        UtilidadesGithubRelease.descargaNuevaVersion(this);
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
            actualizarPanelCentral();
        }
    }

    public void actualizarPanelCentral() {
        this.remove(panelCentral);
        cargarPanelCentral();
        super.add(panelCentral, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(panelCentral);
    }

    private void exportarConfiguracion() {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new JSonFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int retorno = fc.showSaveDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!Objects.equals(FilenameUtils.getExtension(file.getName()), "json")) {
                file = new File(file + ".json");
            }
            UtilidadesConfiguracion.guardarConfiguracion(configuracion, file);
        }
    }

    private void abrirConfiguracion() {
        ConfiguracionUI configuracionUI = new ConfiguracionUI(this, configuracion);
        configuracionUI.setVisible(true);
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

    public S3Folder getRaiz() {
        return raiz;
    }

}
