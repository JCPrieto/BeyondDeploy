package es.jklabs.json.configuracion;

import java.io.Serializable;

public class BucketConfig implements Serializable {

    private static final long serialVersionUID = -6696529132684442401L;
    private String nombre;
    private String usuario;
    private String password;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
