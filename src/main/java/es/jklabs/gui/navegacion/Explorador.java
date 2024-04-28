package es.jklabs.gui.navegacion;

import com.amazonaws.services.s3.model.ObjectListing;
import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.layout.WrapLayout;
import es.jklabs.gui.utilidades.listener.S3DragAndDropListener;
import es.jklabs.gui.utilidades.listener.S3FileListener;
import es.jklabs.gui.utilidades.listener.S3FolderListener;
import es.jklabs.gui.utilidades.task.ExploradorReloader;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;
import es.jklabs.utilidades.UtilsCache;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.stream.Collectors;

public class Explorador extends JPanel {

    private static final long serialVersionUID = -8285796640106146202L;
    public static final String SUBIR_ARCHIVO = "subir.archivo";
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private MainUI padre;
    private Explorador anterior;
    private S3Folder folder;
    private transient Timer timer;
    private JScrollPane scrollPane;

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
        scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
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
        JButton jbUpload = new JButton(mensajes.getString(SUBIR_ARCHIVO));
        jbUpload.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/upload.png"))));
        jbUpload.addActionListener(l -> uploadFile());
        botonera.add(jbUpload, BorderLayout.EAST);
        add(botonera, BorderLayout.NORTH);
    }

    private void uploadFile() {
        JFileChooser fc = new JFileChooser(UtilsCache.getLastUploadFolder());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retorno = fc.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            UtilsCache.setLastUploadFolder(file.getParentFile().getPath());
            padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.setEnabled(false);
            if (UtilidadesS3.uploadFile(file, folder.getFullpath(), padre.getConfiguracion())) {
                Growls.mostrarInfo("subida.realizada");
            } else {
                Growls.mostrarAviso(SUBIR_ARCHIVO, SUBIR_ARCHIVO);
            }
            this.setEnabled(true);
            padre.setCursor(null);
            recargarPantalla();
        }
    }

    public void recargarPantalla() {
        padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        JPanel jpMenu = new JPanel(new WrapLayout(FlowLayout.LEFT));
        jpMenu.setDropTarget(new DropTarget(jpMenu, new S3DragAndDropListener(this)));
        folder.getS3Forlders().forEach(s -> jpMenu.add(getFolder(s)));
        folder.getS3Files().forEach(s -> jpMenu.add(getFile(s)));
        scrollPane.setViewportView(jpMenu);
    }

    private JButton getFile(S3File s3File) {
        JButton jButton = new JButton(s3File.getName());
        jButton.setPreferredSize(new Dimension(110, 100));
        jButton.setContentAreaFilled(false);
        jButton.setToolTipText(s3File.getName());
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
        jButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (icono))));
        jButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton.addMouseListener(new S3FileListener(padre, this, jButton, s3File));
        return jButton;
    }

    private JButton getFolder(S3Folder s3Folder) {
        JButton jButton = new JButton(s3Folder.getName());
        jButton.setPreferredSize(new Dimension(110, 100));
        jButton.setContentAreaFilled(false);
        jButton.setToolTipText(s3Folder.getName());
        jButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/folder-blue.png"))));
        jButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton.addMouseListener(new S3FolderListener(padre, this, jButton, s3Folder));
        return jButton;
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

    public MainUI getPadre() {
        return padre;
    }

    public void uploadFile(List<File> files) {
        padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        this.setEnabled(false);
        List<File> errors = UtilidadesS3.uploadFile(files, folder.getFullpath(), padre.getConfiguracion());
        if (errors.isEmpty()) {
            Growls.mostrarInfo("subida.realizada");
        } else if (files.size() != errors.size()) {
            Growls.mostrarAviso("subida.parcial", "subida.parcial", new String[]{StringUtils.joinWith(", ", errors.stream()
                    .map(File::getName)
                    .collect(Collectors.toList()))});
        } else {
            Growls.mostrarAviso(SUBIR_ARCHIVO, SUBIR_ARCHIVO);
        }
        this.setEnabled(true);
        padre.setCursor(null);
        recargarPantalla();
    }
}
