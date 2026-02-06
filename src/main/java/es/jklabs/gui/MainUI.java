package es.jklabs.gui;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import es.jklabs.gui.configuracion.ConfiguracionUI;
import es.jklabs.gui.configuracion.SecureStorageUI;
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
import java.io.Serial;
import java.util.Objects;

public class MainUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 7929656351162697237L;
    private Configuracion configuracion;
    private JPanel panelCentral;
    private S3Folder raiz;
    private JProgressBar progressBar;
    private Timer progressHideTimer;

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
        UtilidadesS3.setProgressHandler(new UiProgressHandler());
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
        super.add(crearPanelEstado(), BorderLayout.SOUTH);
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
        JMenuItem jmiAlmacenamientoSeguro = new JMenuItem(Mensajes.getMensaje("almacenamiento.seguro"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/secure.png"))));
        jmiAlmacenamientoSeguro.addActionListener(al -> abrirAlmacenamientoSeguro());
        jmArchivo.add(jmiConfiguracion);
        jmArchivo.add(jmiExportar);
        jmArchivo.add(jmiImportar);
        jmArchivo.add(jmiAlmacenamientoSeguro);
        JMenu jmAyuda = new JMenu(Mensajes.getMensaje("ayuda"));
        jmAyuda.setMargin(new Insets(5, 5, 5, 5));
        JMenuItem jmiAcercaDe = new JMenuItem(Mensajes.getMensaje("acerca.de"), new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/info.png"))));
        jmiAcercaDe.addActionListener(al -> mostrarAcercaDe());
        jmAyuda.add(jmiAcercaDe);
        menu.add(jmArchivo);
        menu.add(jmAyuda);
        super.setJMenuBar(menu);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return UtilidadesGithubRelease.existeNuevaVersion();
                } catch (IOException e) {
                    Logger.error("consultar.nueva.version", e);
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        menu.add(Box.createHorizontalGlue());
                        JMenuItem jmActualizacion = new JMenuItem(Mensajes.getMensaje("existe.nueva.version"), new ImageIcon
                                (Objects.requireNonNull(getClass().getClassLoader().getResource("img/icons/update.png"))));
                        jmActualizacion.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                        jmActualizacion.setHorizontalTextPosition(SwingConstants.RIGHT);
                        jmActualizacion.addActionListener(al -> descargarNuevaVersion());
                        menu.add(jmActualizacion);
                        menu.revalidate();
                        menu.repaint();
                    }
                } catch (Exception e) {
                    Logger.error("consultar.nueva.version", e);
                }
            }
        }.execute();
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

    private void abrirAlmacenamientoSeguro() {
        SecureStorageUI ui = new SecureStorageUI(this);
        ui.setVisible(true);
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

    private JPanel crearPanelEstado() {
        JPanel panelEstado = new JPanel(new BorderLayout());
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Listo");
        progressBar.setValue(0);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 18));
        panelDerecha.add(progressBar);
        panelEstado.add(panelDerecha, BorderLayout.EAST);
        return panelEstado;
    }

    private void actualizarProgresoInicio(String accion, String nombre) {
        SwingUtilities.invokeLater(() -> {
            if (progressBar == null) {
                return;
            }
            detenerOcultado();
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            progressBar.setString(accion + ": " + nombre);
            progressBar.setVisible(true);
        });
    }

    private void actualizarProgreso(int porcentaje, String accion, String nombre) {
        SwingUtilities.invokeLater(() -> {
            if (progressBar == null) {
                return;
            }
            detenerOcultado();
            progressBar.setIndeterminate(false);
            progressBar.setValue(porcentaje);
            progressBar.setString(accion + " " + porcentaje + "%: " + nombre);
            progressBar.setVisible(true);
        });
    }

    private void actualizarProgresoFin(boolean ok, String accion, String nombre) {
        SwingUtilities.invokeLater(() -> {
            if (progressBar == null) {
                return;
            }
            progressBar.setIndeterminate(false);
            progressBar.setValue(ok ? 100 : 0);
            progressBar.setString(accion + (ok ? " completada: " : " fallida: ") + nombre);
            programarOcultado();
        });
    }

    private void detenerOcultado() {
        if (progressHideTimer != null && progressHideTimer.isRunning()) {
            progressHideTimer.stop();
        }
    }

    private void programarOcultado() {
        detenerOcultado();
        progressHideTimer = new Timer(1500, e -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.setValue(0);
                progressBar.setString("Listo");
            }
        });
        progressHideTimer.setRepeats(false);
        progressHideTimer.start();
    }

    private class UiProgressHandler implements UtilidadesS3.ProgressHandler {
        @Override
        public void onStart(String accion, String nombre) {
            actualizarProgresoInicio(accion, nombre);
        }

        @Override
        public void onProgress(int porcentaje, String accion, String nombre) {
            actualizarProgreso(porcentaje, accion, nombre);
        }

        @Override
        public void onFinish(boolean ok, String accion, String nombre) {
            actualizarProgresoFin(ok, accion, nombre);
        }
    }
}
