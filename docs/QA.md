# Matriz de QA — MPAZ PRO 2° (app conectada al backend)

Probar desde un **clon limpio** (git clone → Android Studio → Run) con el backend encendido. Marcar ✔ / ✘ y anotar el dispositivo.

| Entorno | Dispositivo / emulador | Fecha | Responsable |
|---|---|---|---|
| A. Android 10 (API 29) | [celular / emulador] | | |
| B. Android 13+ (API 33+) | [emulador / equipo de compañero / laboratorio] | | |

## Antes de cada ronda

1. **Backend:** MySQL encendido, `build-all.bat` y `docker compose up -d` (ver `docs/CONEXION_BACKEND.md`). Para empezar de cero: `DROP DATABASE` de los cinco esquemas y `docker compose restart`.
2. **Pruebas de la API (opcional, desde `postman`):** `npx newman run` de la fase 2 (**una sola vez** sobre datos nuevos), fase 3 y fase 4 → 37, 77 y 100 pruebas sin fallos.
3. **App:** desinstalar la app (base limpia) y ejecutar `./gradlew testDebugUnitTest` → **291 pruebas deben pasar**. El número es el conteo de `@Test` a la fecha; si cambia, actualizarlo aquí.
4. Emulador: la app apunta a `http://10.0.2.2:9090/`. Celular: ver la tabla de `docs/CONEXION_BACKEND.md`.
5. Usuarios (PIN `1234`): `utp`, `docente`, estudiantes `ana`, `beto`, `carla`, `gael` (2° A) y `diego`, `eva`, `isidora`, `felipe` (2° B).

## Casos

### 1. Acceso y apariencia

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 1 | Abrir app | Bienvenida animada; botón «Entrar» legible | | |
| 2 | Login vacío | Errores con ícono en usuario y PIN | | |
| 3 | Login con PIN malo (`utp` / `0000`) | Mensaje con ícono, sin entrar | | |
| 4 | Login `utp` / `1234` | Panel UTP | | |
| 5 | Backend apagado e intentar entrar | Mensaje de que no hay conexión; la app no se cierra | | |
| 6 | Contraste login y panel (claro y modo oscuro) | Todo legible en ambos | | |
| 7 | Chips de asignatura (4 colores) | Texto oscuro legible sobre cada tiza | | |
| 8 | Rotar (Panel, Login, Bienvenida, Formulario, Prueba, Informe) | Nada cortado; texto escrito se conserva | | |
| 9 | Barras del sistema (3 botones en API 29; gestos en 33+) | Contenido no tapado | | |
| 10 | Transiciones al avanzar y volver | Deslizamiento + fundido ~300 ms | | |
| 11 | Cerrar sesión y entrar con otro rol | Cada rol ve solo lo suyo | | |

### 2. UTP (catálogo y estados)

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 12 | «Catálogo del servidor» → Actualizar | Se ve el catálogo con los 5 estados (LEN-OA01 y MAT-OA01 publicados, CIE-OA01 autorizado, HIS-OA01 en revisión, LEN-OA02 borrador) | | |
| 13 | UTP: nueva unidad vacía | Campos con error e ícono | | |
| 14 | UTP: nueva unidad válida | Vuelve a la lista con la tarjeta | | |
| 15 | Enviar a revisión → autorizar → publicar | El estado cambia en cada paso; historial con fechas | | |
| 16 | UTP: rechazar | Vuelve a Borrador; historial | | |
| 17 | UTP: editor de preguntas (vacío) | Errores por campo; no guarda | | |
| 18 | **Recurso nativo 1**: elegir imagen de apoyo | Selector abre; la imagen se ve con borde redondeado | | |
| 19 | Cerrar y reabrir app | Unidades, imagen y estados persisten | | |

### 3. Docente (asignar, monitorear, informar)

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 20 | Asignar sin confirmar / sin fechas | Errores (con ícono) en fechas, intentos, checkbox | | |
| 21 | Asignar con «hasta» < «desde» | Error «no puede ser anterior» | | |
| 22 | Asignación válida (2° A, 2 intentos, activa) | Aparece en «vigentes» y queda en el servidor | | |
| 23 | Docente solo ve objetivos AUTORIZADOS o PUBLICADOS | No aparecen borrador ni en revisión | | |
| 24 | Lista de informes | Cada estudiante con avance, última conexión y última sincronización | | |
| 25 | `beto` (10 días sin conectarse) | Mensaje «Lleva N días sin conectarse.» | | |
| 26 | Informe de `ana` | Mejor porcentaje de sus intentos aceptados; nivel por regla fija (80 / 60); sin diagnóstico automático | | |
| 27 | Observación vacía / muy larga | Error con ícono | | |
| 28 | Observación válida | Se guarda sin validar el informe; reaparece | | |
| 29 | Validar informe | Deja de decir «Pendiente de validación» | | |
| 30 | **Recurso nativo 2**: «Activar avisos» | **API 29:** sin diálogo, avisos activos. **API 33+:** diálogo de permiso | | |
| 31 | Permiso denegado y guardar observación | No falla ni se cierra la app | | |
| 32 | Reiniciar progreso e intentos (con confirmación) | Se archiva el avance y los intentos del estudiante; puede rendir de nuevo | | |

### 4. Estudiante con conexión

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 33 | `ana` (2° A) | Solo unidades publicadas y asignadas; cada una con «Intentos: X de N» | | |
| 34 | Unidad sin asignar (por ejemplo Ciencias o Historia) | No aparece | | |
| 35 | Estudiante de un curso sin asignaciones | «Todavía no hay evaluaciones disponibles.» | | |
| 36 | Abrir un contenido | Texto + imagen; queda anotado como completado | | |
| 37 | Practicar actividades de LEN-OA01 | Una pregunta a la vez (los 4 tipos); correcta/incorrecta con ícono; al terminar muestra aciertos | | |
| 38 | Avance del estudiante | Sube con contenidos, actividades y desafíos (dificultad 3) completados; la prueba no cuenta | | |
| 39 | Docente ve el avance de ese estudiante | Coincide con lo que hizo (se envió a `/progreso/sync`) | | |
| 40 | Rendir la prueba (LEN-OA01) | Preguntas en orden; al terminar, porcentaje y «intentos restantes» | | |
| 41 | Agotar los intentos | «Ya usaste tus N intentos. Habla con tu profesora o profesor.» | | |
| 42 | Prueba con ventana aún no abierta / ya cerrada | «La prueba se abre el …» / «El plazo … terminó el …» | | |
| 43 | Estudiante no ve informes | Sin acceso por UI ni navegación | | |

### 5. Sin conexión y sincronización

Preparación: con red, entrar como estudiante y abrir la lista de unidades, la prueba y las actividades de LEN-OA01 (así quedan guardadas). Luego activar modo avión (o apagar el backend).

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 44 | Lista de unidades sin red | Siguen apareciendo las asignaciones guardadas | | |
| 45 | Practicar actividades sin red | Funcionan con lo guardado; al terminar una se anota como completada | | |
| 46 | Rendir la prueba sin red | Se puede rendir; al terminar: «Tu intento quedó guardado en este teléfono. Se enviará cuando haya conexión.» | | |
| 47 | Intentos usados sin red | El intento guardado ya cuenta como usado (se descuenta de los que quedan) | | |
| 48 | Prueba sin haberla abierto antes con red | Mensaje de que no hay conexión (no hay nada guardado) | | |
| 49 | Volver la red (con la app abierta o al abrirla) | El intento y el avance se envían solos, primero los intentos | | |
| 50 | Docente después de sincronizar | Ve el intento, el avance actualizado y la nueva «Última sincronización» | | |
| 51 | Rendir más intentos sin red que el límite | Al sincronizar solo valen los primeros N (por fecha); el resto se rechaza | | |
| 52 | Cerrar la app con un intento pendiente y volver a abrirla con red | El intento no se pierde y se envía | | |
| 53 | Borrar los datos de la app con un intento sin enviar | Se pierde definitivamente (así lo define el Registro de Decisiones) | | |

## Diferencias esperadas entre entornos
- Caso 18: en API 29 el selector puede ser el de archivos de respaldo; en 33+ el del sistema.
- Caso 30: el diálogo de permiso **solo** existe en API 33+. Es el que se demuestra en la defensa.
- Caso 9: botones de navegación vs gestos.
- Casos 44 a 53: el envío periódico en segundo plano puede tardar hasta unos 15 minutos; para probar rápido, abrir la app con red.

## Errores encontrados
| # | Caso | Descripción | Estado |
|---|---|---|---|
| | | | |
