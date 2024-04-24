package es.jklabs.gui.utilidades.listener;

import es.jklabs.gui.navegacion.Explorador;
import es.jklabs.gui.utilidades.Growls;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;

public class S3DragAndDropListener implements DropTargetListener {
    private final Explorador explorador;

    public S3DragAndDropListener(Explorador explorador) {
        this.explorador = explorador;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = dtde.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        // Iterar sobre los sabores de datos para encontrar los archivos
        for (DataFlavor flavor : flavors) {
            try {
                if (flavor.isFlavorJavaFileListType()) {
                    explorador.uploadFile((java.util.List<File>) transferable.getTransferData(flavor));
                }
            } catch (IOException | UnsupportedFlavorException e) {
                Growls.mostrarError("drag.file", e);
            }
        }
        dtde.dropComplete(true);
    }
}
