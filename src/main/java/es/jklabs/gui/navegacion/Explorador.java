package es.jklabs.gui.navegacion;

import com.amazonaws.services.s3.model.ObjectListing;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.listener.S3FileListener;
import es.jklabs.gui.utilidades.listener.S3FolderListener;
import es.jklabs.gui.utilidades.task.ExploradorReloader;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;

public class Explorador extends JPanel {

    private static final long serialVersionUID = -8285796640106146202L;
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private MainUI padre;
    private Explorador anterior;
    private S3Folder folder;
    private JPanel jpMenu;
    private transient Timer timer;

    public Explorador(MainUI padre, S3Folder folder) {
        super();
        this.padre = padre;
        this.folder = folder;
        setLayout(new BorderLayout());
        cargarElementos();
    }

    public Explorador(MainUI padre, S3Folder s3Folder, Explorador anterior) {
        this(padre, s3Folder);
        this.anterior = anterior;
    }

    private void cargarElementos() {
        cargarBotoneraSuperior();
        timer = new Timer();
        timer.schedule(new ExploradorReloader(this), 0, 60000);
    }

    private void cargarBotoneraSuperior() {
        JPanel botonera = new JPanel();
        botonera.setLayout(new BorderLayout());
        if (!Objects.equals(folder, padre.getRaiz())) {
            JButton jbAtras = new JButton(mensajes.getString("atras"));
            ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                    ("img/icons/back.png")));
            Image img = imageIcon.getImage().getScaledInstance(24, 24, Image
                    .SCALE_SMOOTH);
            jbAtras.setIcon(new ImageIcon(img));
            jbAtras.addActionListener(l -> retroceder());
            botonera.add(jbAtras, BorderLayout.WEST);
            JLabel jlNombreCarpera = new JLabel(folder.getName(), SwingConstants.CENTER);
            botonera.add(jlNombreCarpera, BorderLayout.CENTER);
        }
        JButton jbUpload = new JButton(mensajes.getString("subir.archivo"));
        jbUpload.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/upload.png"))));
        jbUpload.addActionListener(l -> uploadFile());
        botonera.add(jbUpload, BorderLayout.EAST);
        add(botonera, BorderLayout.NORTH);
    }

    private void uploadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retorno = fc.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.setEnabled(false);
            UtilidadesS3.uploadFile(file, folder.getFullpath(), padre.getConfiguracion().getBucketConfig());
            Growls.mostrarInfo("subida.realizada");
            this.setEnabled(true);
            padre.setCursor(null);
            recargarPantalla();
        }
    }

    public void recargarPantalla() {
        padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (jpMenu != null) {
            remove(jpMenu);
        }
        folder.getS3Forlders().clear();
        folder.getS3Files().clear();
        try {
            ObjectListing elementos = UtilidadesS3.getObjetos(padre.getConfiguracion().getBucketConfig(), folder
                    .getFullpath());
            UtilidadesS3.actualizarCarpeta(folder, elementos);
            cargarPanelCentral();
            if (padre.getPanelCentral() != null) {
                SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(padre.getPanelCentral()));
            }
        } catch (Exception e) {
            Growls.mostrarError("cargar.archivos.bucket", e);
        }
        padre.setCursor(null);
    }

    private void retroceder() {
        timer.cancel();
        timer.purge();
        padre.remove(this);
        anterior.recargarPantalla();
        padre.setPanelCentral(anterior);
        folder.getS3Forlders().clear();
        folder.getS3Files().clear();
        padre.add(anterior, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(padre.getPanelCentral());
    }

    private void cargarPanelCentral() {
        jpMenu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        folder.getS3Forlders().forEach(this::addCarpeta);
        folder.getS3Files().forEach(this::addObjeto);
        add(jpMenu, BorderLayout.CENTER);
    }

    private void addObjeto(S3File s3File) {
        JLabel jLabel = new JLabel(s3File.getName());
        String icono;
        if (esArchivoComprimido(s3File.getName())) {
            icono = "img/icons/compress.png";
        } else if (esArchivoExcel(s3File.getName())) {
            icono = "img/icons/excel.png";
        } else if (esArchivoXML(s3File.getName())) {
            icono = "img/icons/xml.png";
        } else {
            icono = "img/icons/file.png";
        }
        jLabel.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (icono))));
        jLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabel.addMouseListener(new S3FileListener(padre, this, jLabel, s3File));
        jpMenu.add(jLabel);
    }

    private boolean esArchivoXML(String name) {
        return name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".xml");
    }

    private boolean esArchivoExcel(String name) {
        return name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv");
    }

    private boolean esArchivoComprimido(String name) {
        return name.endsWith(".war") || name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar.gz");
    }

    private void addCarpeta(S3Folder s3Folder) {
        JLabel jLabel = new JLabel(s3Folder.getName());
        jLabel.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/folder-blue.png"))));
        jLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabel.addMouseListener(new S3FolderListener(padre, this, jLabel, s3Folder));
        jpMenu.add(jLabel);
    }

    public MainUI getPadre() {
        return padre;
    }

}
