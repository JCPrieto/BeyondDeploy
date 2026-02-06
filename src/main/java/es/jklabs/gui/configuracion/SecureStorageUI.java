package es.jklabs.gui.configuracion;

import es.jklabs.utilidades.Mensajes;
import es.jklabs.utilidades.SecureStorageManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class SecureStorageUI extends JDialog {

    @Serial
    private static final long serialVersionUID = 6421120324107728751L;

    private final SecureStorageManager storageManager;
    private final ProviderTableModel tableModel;

    public SecureStorageUI(JFrame owner) {
        super(owner, Mensajes.getMensaje("almacenamiento.seguro.titulo"), true);
        this.storageManager = SecureStorageManager.getInstance();
        this.tableModel = new ProviderTableModel(storageManager);
        construirUI();
        setSize(520, 360);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(Mensajes.getMensaje("almacenamiento.seguro.tab.contrasena"), buildProviderPanel());
        tabs.addTab(Mensajes.getMensaje("almacenamiento.seguro.tab.contenido"), buildInfoPanel());
        tabs.addTab(Mensajes.getMensaje("almacenamiento.seguro.tab.avanzado"), buildActionsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildProviderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        TableColumn prioCol = table.getColumnModel().getColumn(2);
        prioCol.setCellRenderer(new DefaultTableCellRenderer());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton(Mensajes.getMensaje("almacenamiento.seguro.recargar"));
        refresh.addActionListener(e -> tableModel.reload());
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea info = new JTextArea(Mensajes.getMensaje("almacenamiento.seguro.info"));
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        panel.add(new JScrollPane(info), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton limpiar = new JButton(Mensajes.getMensaje("almacenamiento.seguro.limpiar.cache"));
        limpiar.addActionListener(e -> storageManager.clearCachePublic());
        JButton cambiar = new JButton(Mensajes.getMensaje("almacenamiento.seguro.cambiar.password"));
        cambiar.addActionListener(e -> storageManager.rotatePassword());
        JButton recuperar = new JButton(Mensajes.getMensaje("almacenamiento.seguro.recuperar.password"));
        recuperar.addActionListener(e -> JOptionPane.showMessageDialog(this,
                Mensajes.getMensaje("almacenamiento.seguro.recuperar.no.disponible"),
                Mensajes.getMensaje("almacenamiento.seguro.titulo"), JOptionPane.INFORMATION_MESSAGE));
        buttons.add(limpiar);
        buttons.add(cambiar);
        buttons.add(recuperar);
        panel.add(buttons, BorderLayout.NORTH);
        JButton cerrar = new JButton(Mensajes.getMensaje("cerrar"));
        cerrar.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cerrar);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private static class ProviderTableModel extends AbstractTableModel {

        private final SecureStorageManager storageManager;
        private final List<SecureStorageManager.ProviderInfo> data = new ArrayList<>();

        ProviderTableModel(SecureStorageManager storageManager) {
            this.storageManager = storageManager;
            reload();
        }

        void reload() {
            data.clear();
            data.addAll(storageManager.getProviderInfo());
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> Mensajes.getMensaje("almacenamiento.seguro.col.activo");
                case 1 -> Mensajes.getMensaje("almacenamiento.seguro.col.proveedor");
                case 2 -> Mensajes.getMensaje("almacenamiento.seguro.col.prioridad");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SecureStorageManager.ProviderInfo info = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> info.isEnabled();
                case 1 -> label(info.getType()) + (info.isActive() ? " *" : "");
                case 2 -> info.getPriority();
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return data.get(rowIndex).isAvailable();
            }
            return columnIndex == 2;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            SecureStorageManager.ProviderInfo info = data.get(rowIndex);
            if (columnIndex == 0) {
                if (!(aValue instanceof Boolean)) {
                    return;
                }
                boolean enabled = (Boolean) aValue;
                SecureStorageManager.UpdateResult result = storageManager.updateProviderConfigInternal(info.getType(),
                        enabled, info.getPriority());
                if (reaload(result)) {
                    return;
                }
                return;
            }
            if (columnIndex == 2) {
                try {
                    int priority = Integer.parseInt(aValue.toString());
                    SecureStorageManager.UpdateResult result = storageManager.updateProviderConfigInternal(info.getType(),
                            info.isEnabled(), priority);
                    reaload(result);
                } catch (NumberFormatException ignored) {
                    // no-op
                }
            }
        }

        private boolean reaload(SecureStorageManager.UpdateResult result) {
            if (!result.isApplied()) {
                JOptionPane.showMessageDialog(null, Mensajes.getMensaje(result.getErrorKey()),
                        Mensajes.getMensaje("almacenamiento.seguro.titulo"), JOptionPane.WARNING_MESSAGE);
                reload();
                return true;
            }
            if (result.hasPriorityTie()) {
                JOptionPane.showMessageDialog(null, Mensajes.getMensaje("almacenamiento.seguro.validacion.prioridad.empate"),
                        Mensajes.getMensaje("almacenamiento.seguro.titulo"), JOptionPane.WARNING_MESSAGE);
            }
            reload();
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        private String label(SecureStorageManager.ProviderType type) {
            return switch (type) {
                case LINUX_SECRET_TOOL -> "Secret Service";
                case WINDOWS_CREDENTIAL_MANAGER -> "Windows Credential Manager";
                case MACOS_KEYCHAIN -> "macOS Keychain";
                case PASSWORD_PBKDF2 -> "Password Prompt";
                case SYSTEM_PROPERTY -> "System Property";
            };
        }
    }
}
