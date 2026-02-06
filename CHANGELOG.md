# Changelog

## 0.13.0 - 2026-02-06

- Nuevo almacenamiento seguro de credenciales con proveedores nativos por sistema operativo (Windows Credential Manager,
  macOS Keychain, Linux Secret Tool) y fallback con password PBKDF2.
- Migracion automatica de datos legados al nuevo vault cifrado (AES-GCM) con metadatos de proveedor y gestion de
  rotacion de password.
- Nueva UI de configuracion de almacenamiento seguro e integracion en la configuracion principal para gestionar
  proveedor activo y disponibilidad.
- Refactor de descargas de GitHub Releases con mejor separacion de responsabilidades, inyeccion para pruebas, resolucion
  robusta de assets/version y manejo de errores.
- Dependencias ampliadas con JNA/JNA Platform para soporte multiplataforma de almacenamiento seguro y tests unitarios
  actualizados.

## 0.12.0 - 2026-01-28

- Barra de progreso visible solo durante transferencias y refresco incremental en subidas/descargas.
- Subidas/descargas en background para evitar bloqueos de UI.
- Reutilizacion de cliente S3 y TransferManager para transferencias grandes.
- Actualizacion de dependencias (Jackson, JUnit, SonarQube) y wrapper de Gradle 9.3.0.
- Tests unitarios ampliados para utilidades S3 y flujo de subidas en Explorador.

## 0.11.0 - 2026-01-27

- Requisito de Java actualizado a 21 y toolchain ajustada en Gradle.
- Paginacion de versiones en S3 con carga bajo demanda en el menu contextual.
- Manejo de errores S3/GitHub mas detallado y mensajes de usuario ampliados.
- Tests unitarios para cifrado y utilidades S3 con soporte Mockito.

## 0.10.1

- Inicializacion de UI en EDT y tareas de red/configuracion en background para evitar bloqueos.
- Manejo tolerante de cambios en la API de GitHub Releases (campos desconocidos).
- Desactivacion del aviso de deprecacion del AWS SDK v1 en arranque.

## 0.10.0

- Migracion del sistema de actualizacion a GitHub Releases.
- Eliminacion de dependencia y credenciales de Firebase.
- Workflow de tests y publicacion de releases.
- Version centralizada en Gradle.

## 0.9.1

- Actualizacion de dependencias: commons-lang3 a 3.18.0 y firebase-admin a 9.7.0.

## 0.9.0

- Drag and drop de archivos.
- Homogeneizacion del tamano de los elementos del explorador de archivos y carpetas.
- Scroll vertical cuando es necesario.
- Guardado de la ruta del ultimo archivo subido para futuras subidas.
- Correciones de seguridad y estabilidad.

## 0.8.0

- Opcion de descarga de una version anterior.
- Mensaje de confirmacion antes de eliminar un archivo o version.

## 0.7.0

- Mostrar versiones anteriores de un archivo para volver a una version anterior.

## 0.6.0

- Permitir dar permisos a otras cuentas AWS para acceso a los archivos subidos al bucket.

## 0.5.4

- Correciones de seguridad y estabilidad.

## 0.5.3

- Correciones de seguridad.

## 0.5.2

- Actualizacion de seguridad de dependencias.

## 0.5.1

- Actualizacion de seguridad de la libreria de Jackson.

## 0.5.0

- Eliminacion de dependencia de Java-Gnome por innecesaria.

## 0.4.1

- Compatibilidad con entornos de escritorio Linux no GTK.

## 0.4.0

- A partir de esta version es necesario Java 11.
- Eliminacion de ControlFX por incompatibilidades con OpenJDK 11.
- Uso de Systray en Windows y Java-Gnome en Linux (requiere libNotify en el sistema).

## 0.3.1

- Correccion de error al tratar los archivos JSON de configuracion.

## 0.3.0

- Eliminacion del icono de Systray por incompatibilidad con Gnome3.
- Uso de ControlsFX para mostrar notificaciones.

## 0.2.2

- Icono para archivos comprimidos.
- Cambio de color de iconos de subida y bajada para fondos claros.

## 0.2.1

- Eliminacion de archivos de logs vacios.

## 0.2.0

- Anadir menu contextual en archivos para descargarlos o eliminarlos.

## 0.1.1

- Mostrar cursor de ocupado mientras se sube un archivo.

## 0.1.0

- Sincronizacion continua de las carpetas.
