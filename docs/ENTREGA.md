# Lista de verificación de entrega (pasos humanos)

> Regla de oro: **después de la entrega no se toca el repositorio** (ni un commit, ni un README). Cualquier cambio = nota 1.0.
> Entrega: antes de la primera sesión del Parcial 3 (semana 11).

## 1. Antes de subir (en el equipo de cada uno)
- [ ] `git clone` limpio en otra carpeta; abre en Android Studio; sincroniza y ejecuta (clon limpio, no tu carpeta de trabajo).
- [ ] Backend arriba en tu equipo (`docs/CONEXION_BACKEND.md`): MySQL, `build-all.bat`, `docker compose up -d`; newman de las fases 2, 3 y 4 sin fallos (37, 77 y 100 pruebas). La fase 2 se corre **una sola vez** sobre datos nuevos.
- [ ] `./gradlew testDebugUnitTest`: 291 pruebas en verde (no necesitan el backend). Si el número cambió, actualizarlo aquí, en `README.md` y en `docs/QA.md`.
- [ ] QA completo en `docs/QA.md` en **API 29** y en **API 33+** (el diálogo de permiso solo existe en 33+), incluido el bloque 5 (sin conexión y sincronización). Anotar los fallos en «Errores encontrados» y corregirlos antes de entregar.
- [ ] Revisar contraste en modo claro y oscuro (casos 6 y 7 del QA).
- [ ] Completar README: nombres, roles, N° de equipo, enlace de Trello.
- [ ] Datos 100 % ficticios; sin claves ni keystore en el repo (`.gitignore` ya excluye `local.properties`, `*.jks`).
- [ ] Sin rutas de tu computador en archivos del repo (por ejemplo `org.gradle.java.home` en `gradle.properties`) y sin el `.env` del backend (lleva el `SECRET`).
- [ ] Sin IA generativa, sin micrófono y sin datos reales de estudiantes (restricciones del caso).

## 2. GitHub
- [ ] Repositorio **público**; invitar al docente y al compañero como colaboradores.
- [ ] Subir todo el código (incluye `gradle/`, `gradlew`, `docs/`, `.github/`); **no** subir `build/` ni `local.properties`.
- [ ] Historial con commits técnicos (`feat:`, `fix:`…) de **ambos** integrantes.
- [ ] Ramas `feature/*` con pull requests revisados por el otro, si es posible.
- [ ] Verificar que el README se ve bien en la página del repositorio.
- [ ] Crear la etiqueta: `git tag -a v1.0-parcial2 -m "Entrega Parcial 2" && git push origin v1.0-parcial2`.

## 3. Trello
- [ ] Tablero con columnas Pendiente, En curso, En revisión, Hecho.
- [ ] Tarjetas de las fases 3 a 8, asignadas a cada integrante; las terminadas en «Hecho».
- [ ] Actividad real de ambos; enlace pegado en el README (antes de la etiqueta).
- [ ] Tablero visible para el docente (enlace público o invitación).

## 4. AVA (un solo integrante)
- [ ] Comprimir el proyecto **sin** `build/`, `.gradle/`, `.idea/` ni `local.properties`.
- [ ] Verificar que coincide con GitHub en la etiqueta `v1.0-parcial2` (mismo contenido).
- [ ] Subir el zip a la entrega **grupal** en AVA.

## 5. Enlace individual (**cada uno**)
- [ ] Activar el enlace individual en AVA con: nombre, apellido, número de equipo y nombre de la app («MPAZ PRO 2°»). Sin esto baja la nota individual.

## 6. Congelar
- [ ] Confirmar que `main` está en la etiqueta `v1.0-parcial2`.
- [ ] Acordar entre ambos: **ningún push** hasta la evaluación. Ensayar solo en copia local no subida.
- [ ] (Opcional) activar protección de la rama `main` o archivar el repo en modo lectura tras confirmar la entrega.

## 7. Ensayo de defensa
- [ ] El día de la defensa el backend tiene que estar **encendido** (MySQL + Docker) y la app apuntando a él: emulador en `10.0.2.2:9090`; celular físico con `adb reverse tcp:9090 tcp:9090` o por WiFi (ver `docs/CONEXION_BACKEND.md`). Probarlo con anticipación.
- [ ] Ensayar la demostración sin conexión: con red abrir la lista, las actividades y la prueba; modo avión; rendir; volver la red y mostrar que llegó (el docente ve la nueva «Última sincronización»).
- [ ] Tener a mano un plan B si el backend falla: la app abre y deja entrar con la copia local, y se pueden mostrar las pruebas unitarias.
- [ ] Cada uno ensaya `docs/DEFENSA.md` con cronómetro: los 7 ejercicios en < 3 min.
- [ ] Revisión cruzada: cada uno explica al otro **todo** el código (la nota es individual).
- [ ] Llevar el celular con la app instalada y un cable/depuración inalámbrica de respaldo.
- [ ] Preparar el equipo con API 33+ para mostrar el permiso de notificaciones.

## Pendientes abiertos
- **Validar el recorrido UTP → Docente → Estudiante en el emulador** (nunca se ha hecho): contenido, actividades, prueba con y sin red, y envío al volver la red.
- Confirmar con el profesor si el backend (`mpaz-backend`) forma parte de la entrega y en qué formato (zip o repositorio aparte); hoy este repositorio solo contiene la app.
- Conseguir la tabla completa de la pauta del Parcial 2 (llegó cortada).
- Confirmar con el profesor: carpetas `data/local`, `viewmodel/validation`, `ui/navigation`; si el selector de fotos y las notificaciones cuentan como los 2 recursos nativos.
