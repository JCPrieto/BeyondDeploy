# Changelog

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
