package es.jklabs.gui.utilidades.table.model;

import es.jklabs.json.configuracion.CannonicalId;
import es.jklabs.utilidades.Mensajes;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Comparator;
import java.util.List;

public class CannonicalTableModel extends DefaultTableModel implements TableModel {

    private static final long serialVersionUID = 4786748924338489864L;

    public CannonicalTableModel(List<CannonicalId> cannonicalIds) {
        super();
        String[] columnas = {Mensajes.getMensaje("nombre.cuenta"), Mensajes.getMensaje("cannonical.id")};
        if (cannonicalIds != null) {
            cannonicalIds.sort(Comparator.comparing(CannonicalId::getNombre));
            Object[][] data = new Object[cannonicalIds.size()][2];
            for (int i = 0; i < cannonicalIds.size(); i++) {
                data[i][0] = cannonicalIds.get(i).getNombre();
                data[i][1] = cannonicalIds.get(i).getId();
            }
            super.setDataVector(data, columnas);
        } else {
            super.setDataVector(new Object[0][], columnas);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
