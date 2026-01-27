package es.jklabs.gui.menu.contextual;

import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.s3.model.S3File;
import es.jklabs.s3.model.S3FileVersion;
import es.jklabs.utilidades.Logger;
import es.jklabs.utilidades.Mensajes;
import es.jklabs.utilidades.UtilidadesS3;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class S3FilePopUp extends JPopupMenu {

    @Serial
    private static final long serialVersionUID = 5925867780383236170L;
    private static final String ELIMINAR = "eliminar";
    private static final String IMG_ICONS_TRASH_PNG = "img/icons/trash.png";
    private static final int VERSIONES_POR_PAGINA = 10;
    private final Explorador explorador;
    private final S3File s3File;
    private UtilidadesS3.PaginadorVersiones paginadorVersiones;
    private boolean cargando;
    private List<List<S3FileVersion>> paginas;
    private int paginaActual;
    private JMenuItem vacioItem;

    public S3FilePopUp(Explorador explorador, S3File s3File) {
        super();
        this.explorador = explorador;
        this.s3File = s3File;
        cargarElementos();
    }

    private void cargarElementos() {
        ImageIcon iconDownload = new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("img/icons/download.png")));
        JMenuItem jmiDescargar = new JMenuItem(Mensajes.getMensaje("descargar"), iconDownload);
        jmiDescargar.addActionListener(l -> descargarArchivo());
        add(jmiDescargar);
        ImageIcon iconPapelera = getIcon(IMG_ICONS_TRASH_PNG);
        ImageIcon iconReloj = getIcon("img/icons/clock.png");
        JMenu jmVersiones = new JMenu(Mensajes.getMensaje("versiones"));
        jmVersiones.setIcon(iconReloj);
        configurarVersiones(jmVersiones, iconDownload, iconPapelera);
        add(jmVersiones);
        JMenuItem jmiEliminar = new JMenuItem(Mensajes.getMensaje(ELIMINAR), iconPapelera);
        jmiEliminar.addActionListener(l -> elminarArchivo());
        add(jmiEliminar);
    }

    private void configurarVersiones(JMenu jmVersiones, ImageIcon iconDownload, ImageIcon iconPapelera) {
        paginas = new ArrayList<>();
        paginaActual = 0;
        JMenuItem cargandoItem = new JMenuItem(Mensajes.getMensaje("versiones.cargando"));
        cargandoItem.setEnabled(false);
        vacioItem = new JMenuItem(Mensajes.getMensaje("versiones.vacio"));
        vacioItem.setEnabled(false);
        jmVersiones.add(cargandoItem);
        paginadorVersiones = UtilidadesS3.crearPaginadorVersiones(explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File, VERSIONES_POR_PAGINA);
        jmVersiones.getPopupMenu().addMouseWheelListener(event -> {
            if (cargando) {
                return;
            }
            if (event.getWheelRotation() > 0) {
                cargarPaginaSiguiente(jmVersiones, iconDownload, iconPapelera);
            } else if (event.getWheelRotation() < 0) {
                cargarPaginaAnterior(jmVersiones, iconDownload, iconPapelera);
            }
        });
        cargarPrimeraPagina(jmVersiones, iconDownload, iconPapelera);
    }

    private void cargarPrimeraPagina(JMenu jmVersiones, ImageIcon iconDownload, ImageIcon iconPapelera) {
        if (cargando) {
            return;
        }
        cargando = true;
        new SwingWorker<List<S3FileVersion>, Void>() {
            @Override
            protected List<S3FileVersion> doInBackground() {
                return paginadorVersiones.nextPage();
            }

            @Override
            protected void done() {
                try {
                    List<S3FileVersion> pagina = get();
                    paginas.add(pagina);
                    paginaActual = 0;
                    reconstruirMenu(jmVersiones, iconDownload, iconPapelera);
                } catch (Exception e) {
                    Logger.error("cargar.versiones", e);
                } finally {
                    cargando = false;
                }
            }
        }.execute();
    }

    private void cargarPaginaSiguiente(JMenu jmVersiones, ImageIcon iconDownload, ImageIcon iconPapelera) {
        if (cargando) {
            return;
        }
        int siguiente = paginaActual + 1;
        if (siguiente < paginas.size()) {
            paginaActual = siguiente;
            reconstruirMenu(jmVersiones, iconDownload, iconPapelera);
            return;
        }
        if (!paginadorVersiones.hasMore()) {
            return;
        }
        cargando = true;
        new SwingWorker<List<S3FileVersion>, Void>() {
            @Override
            protected List<S3FileVersion> doInBackground() {
                return paginadorVersiones.nextPage();
            }

            @Override
            protected void done() {
                try {
                    List<S3FileVersion> pagina = get();
                    if (!pagina.isEmpty()) {
                        paginas.add(pagina);
                        paginaActual = paginas.size() - 1;
                    }
                    reconstruirMenu(jmVersiones, iconDownload, iconPapelera);
                } catch (Exception e) {
                    Logger.error("cargar.versiones", e);
                } finally {
                    cargando = false;
                }
            }
        }.execute();
    }

    private void cargarPaginaAnterior(JMenu jmVersiones, ImageIcon iconDownload, ImageIcon iconPapelera) {
        if (cargando || paginaActual <= 0) {
            return;
        }
        paginaActual -= 1;
        reconstruirMenu(jmVersiones, iconDownload, iconPapelera);
    }

    private void reconstruirMenu(JMenu jmVersiones, ImageIcon iconDownload, ImageIcon iconPapelera) {
        jmVersiones.removeAll();
        if (paginas.isEmpty() || paginas.getFirst().isEmpty()) {
            jmVersiones.add(vacioItem);
            return;
        }
        List<S3FileVersion> pagina = paginas.get(paginaActual);
        for (S3FileVersion version : pagina) {
            jmVersiones.add(crearMenuVersion(version, iconDownload, iconPapelera));
        }
        jmVersiones.revalidate();
        jmVersiones.repaint();
    }

    private JMenu crearMenuVersion(S3FileVersion s3FileVersion, ImageIcon iconDownload, ImageIcon iconPapelera) {
        JMenu jmRegVersion = new JMenu(s3FileVersion.getFecha().toString());
        JMenuItem jmiDownloadVersion = new JMenuItem(Mensajes.getMensaje("descargar"), iconDownload);
        JMenuItem jmiDeleteVersion = new JMenuItem(Mensajes.getMensaje(ELIMINAR), iconPapelera);
        jmiDownloadVersion.addActionListener(l -> descargarVersion(s3FileVersion));
        jmiDeleteVersion.addActionListener(l -> eliminarVersion(s3FileVersion));
        jmRegVersion.add(jmiDownloadVersion);
        jmRegVersion.add(jmiDeleteVersion);
        return jmRegVersion;
    }

    private void descargarVersion(S3FileVersion s3FileVersion) {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3FileVersion);
    }

    private ImageIcon getIcon(String resource) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (resource))).getImage().getScaledInstance(24, 24, Image
                .SCALE_SMOOTH));
    }

    private void eliminarVersion(S3FileVersion s3FileVersion) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (IMG_ICONS_TRASH_PNG)));
        int input = JOptionPane.showConfirmDialog(explorador, Mensajes.getMensaje("confirmacion.eliminar", new String[]{s3FileVersion.getFecha().toString()}), Mensajes.getMensaje(ELIMINAR),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (input == JOptionPane.YES_OPTION) {
            UtilidadesS3.elimninarVersion(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File, s3FileVersion);
            explorador.recargarPantalla();
        }
    }

    private void elminarArchivo() {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource
                (IMG_ICONS_TRASH_PNG)));
        int input = JOptionPane.showConfirmDialog(explorador, Mensajes.getMensaje("confirmacion.eliminar", new String[]{s3File.getName()}), Mensajes.getMensaje(ELIMINAR),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (input == JOptionPane.YES_OPTION) {
            UtilidadesS3.deleteObject(explorador.getPadre().getConfiguracion().getBucketConfig(), s3File);
            explorador.recargarPantalla();
        }
    }

    private void descargarArchivo() {
        UtilidadesS3.getObject(explorador.getPadre(), explorador.getPadre().getConfiguracion().getBucketConfig(),
                s3File);
    }
}
