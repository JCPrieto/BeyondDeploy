package es.jklabs.gui.utilidades.task;

import es.jklabs.gui.navegacion.Explorador;

import java.util.Objects;
import java.util.TimerTask;

public class ExploradorReloader extends TimerTask {

    private final Explorador explorador;

    public ExploradorReloader(Explorador explorador) {
        this.explorador = explorador;
    }

    @Override
    public void run() {
        if (Objects.equals(explorador.getPadre().getPanelCentral(), explorador)) {
            explorador.recargarPantalla();
        }
    }
}
