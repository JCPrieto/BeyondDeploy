package es.jklabs.json.configuracion;

import java.io.Serializable;
import java.util.Objects;

public class CannonicalId implements Serializable {

    private static final long serialVersionUID = 3598709215999977450L;
    private String nombre;
    private String id;

    public CannonicalId() {

    }

    public CannonicalId(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CannonicalId that = (CannonicalId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
