package es.jklabs.gui.navegacion;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.Growls;
import es.jklabs.gui.utilidades.listener.S3FileListener;
import es.jklabs.gui.utilidades.listener.S3FolderListener;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class Explorador extends JPanel {

    private static final long serialVersionUID = -8285796640106146202L;
    private static ResourceBundle mensajes = ResourceBundle.getBundle("i18n/mensajes", Locale.getDefault());
    private final MainUI padre;
    private Explorador anterior;
    private S3Folder folder;
    private JPanel jpMenu;

    public Explorador(MainUI padre, S3Folder folder) {
        super();
        this.padre = padre;
        this.folder = folder;
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder(folder.getName()));
        cargarElementos();
    }

    public Explorador(MainUI padre, S3Folder s3Folder, Explorador anterior) {
        this(padre, s3Folder);
        this.anterior = anterior;
    }

    private void cargarElementos() {
        cargarBotoneraSuperior();
        cargarPanelCentral();
    }

    private void cargarBotoneraSuperior() {
        JPanel botonera = new JPanel();
        botonera.setLayout(new BorderLayout());
        if (!Objects.equals(folder, padre.getRaiz())) {
            JButton jbAtras = new JButton(mensajes.getString("atras"));
            ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                    ("img/icons/back.png")));
            Image img = imageIcon.getImage().getScaledInstance(24, 24, java.awt.Image
                    .SCALE_SMOOTH);
            jbAtras.setIcon(new ImageIcon(img));
            jbAtras.addActionListener(l -> retroceder());
            botonera.add(jbAtras, BorderLayout.WEST);
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
            this.setEnabled(false);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (UtilidadesS3.uploadFile(file, folder.getFullpath(), padre.getConfiguracion().getBucketConfig())) {
                S3File nuevo = new S3File(file.getName(), folder.getFullpath() + file.getName());
                folder.getS3Files().add(nuevo);
                this.setEnabled(true);
                this.setCursor(null);
                addObjeto(nuevo);
                SwingUtilities.updateComponentTreeUI(padre.getPanelCentral());
                Growls.mostrarInfo(padre, "subida.realizada");
            }
        }
    }

    private void retroceder() {
        padre.remove(this);
        padre.setPanelCentral(anterior);
        padre.add(anterior, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(padre.getPanelCentral());
    }

    private void cargarPanelCentral() {
        jpMenu = new JPanel();
        jpMenu.setLayout(new GridLayout(0, 5, 10, 10));
        jpMenu.setBorder(new EmptyBorder(10, 10, 10, 10));
        folder.getS3Forlders().forEach(this::addCarpeta);
        folder.getS3Files().forEach(this::addObjeto);
        JScrollPane jScrollPane = new JScrollPane(jpMenu);
        jScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(jScrollPane, BorderLayout.CENTER);
    }

    private void addObjeto(S3File s3File) {
        JLabel jLabel = new JLabel(s3File.getName());
        jLabel.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                ("img/icons/file.png"))));
        jLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabel.addMouseListener(new S3FileListener(padre, this, jLabel, s3File));
        jpMenu.add(jLabel);
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

}
