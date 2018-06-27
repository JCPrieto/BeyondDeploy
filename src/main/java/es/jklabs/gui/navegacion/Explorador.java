package es.jklabs.gui.navegacion;

import es.jklabs.gui.MainUI;
import es.jklabs.gui.utilidades.listener.S3FileListener;
import es.jklabs.gui.utilidades.listener.S3FolderListener;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3Folder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Objects;

public class Explorador extends JPanel {

    private final MainUI padre;
    private final S3Folder folder;
    private JPanel jpMenu;

    public Explorador(MainUI padre, S3Folder folder) {
        super();
        this.padre = padre;
        this.folder = folder;
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder(folder.getName()));
        cargarElementos();
    }

    private void cargarElementos() {
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
