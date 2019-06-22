import org.gnome.gtk.Gtk;
import org.gnome.notify.Notification;
import org.gnome.notify.Notify;

public class HelloWorld {

    public static void main(String[] args) throws InterruptedException {
        Gtk.init(args);
        Notify.init("NombreAPP");
        new Notification("Titulo", "Cuerpo de mensaje informativo", "dialog-information").show();
//        Thread.sleep(5000);
        new Notification("Titulo", "Cuerpo de mensaje de alerta", "dialog-warning").show();
//        Thread.sleep(5000);
        new Notification("Titulo", "Cuerpo de mensaje de error", "dialog-error").show();
    }
}
