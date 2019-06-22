# BeyondDeploy #

Aplicación de uso interno de la empresa BeBeyond para copiar archivos dentro de un bucket de Amazon S3

### Requisitos ###

* Java 11
* LibNotify (Para las notificaciones en Linux)

### Ejecución ###

* Windows:
    * Ejecutar BeyondDeploy.bat dentro del directorio bin

* Linux:
    * Ejecutar BeyondDeploy.sh dentro del directorio bin

### Tecnologías utilizadas ###

* Iconos: Papirus https://github.com/PapirusDevelopmentTeam/papirus-icon-theme
* Librerias:
    * Jackson https://github.com/FasterXML/jackson-core/wiki
    * Firebase https://firebase.google.com
    * AWS Amazon S3 https://aws.amazon.com/sdkforjava
    * Apache Commons Lang http://commons.apache.org/proper/commons-lang
    * Apache Commons IO http://commons.apache.org/proper/commons-io
    
### ToDo ###

* Integración con EC2 para crear, parar o reiniciar maquinas, así como ejecutar sentencias de comando via SSH.

### Changelog ###

* 0.5.0
    * Eliminamos dependencia de Java-Gnome por innecesaria

* 0.4.1
    * Añadimos compatibilidad con entornos de escritorios Linux no GTK.

* 0.4.0
    * A partir de ahora es necesario Java 11.
    * Eliminamos ControlFX por problemas de compatibilidad con OpenJDK 11 y en su lugar utilizamos Systray en S.O Windows
    (sin testear, por lo que es posible que pueda fallar) y Java-Gnome en S.O Linux (No es necesario que el entrono de
    escritorio sea Gnome, con tener libNotify instalado en el sistema, debe funcionar).    

* 0.3.1
    * Correccón de error al tratar los archivos json de configuración.

* 0.3.0
    * Se elimina el icono de Systray, por la incompatibilidad con Gnome3 y utilizamos ControlsFX para monstrar las 
    notificaciones.

* 0.2.2
    * Se añade icono para los archivos comprimidos
    * Cambio del color del icono de subida y bajada de archivos para que se vean mejor sobre fondos claros

* 0.2.1
    * Eliminar archivos de logs vacios

* 0.2.0
    * Añadido menú contextual en los archivos para poder descargarlos o eliminarlos

* 0.1.1
    * Mostrar el cursor de ocupado mientras se esta subiendo un archivo

* 0.1.0
    * Sincronización continua de las carpetas

### Licencia ### 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.