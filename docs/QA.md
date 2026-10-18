# Matriz de QA — MPAZ PRO 2°

Probar desde un **clon limpio** (git clone → Android Studio → Run). Marcar ✔ / ✘ y anotar el dispositivo.

| Entorno | Dispositivo / emulador | Fecha | Responsable |
|---|---|---|---|
| A. Android 10 (API 29) | [celular] | | |
| B. Android 13+ (API 33+) | [emulador / equipo de compañero / laboratorio] | | |

Antes de cada ronda: desinstalar la app (base limpia) y ejecutar `./gradlew testDebugUnitTest` (70 pruebas deben pasar).

## Casos

| # | Caso | Resultado esperado | A (29) | B (33+) |
|---|---|---|---|---|
| 1 | Abrir app | Bienvenida animada; botón «Entrar» legible | | |
| 2 | Login vacío | Errores con ícono en usuario y PIN | | |
| 3 | Login con PIN malo (`utp` / `0000`) | Mensaje con ícono, sin entrar | | |
| 4 | Login `utp` / `1234` | Panel UTP | | |
| 5 | Contraste login y panel (claro) | Todo el texto legible, fondo claro | | |
| 6 | Contraste login y panel (modo oscuro del sistema) | Todo legible, fondo oscuro | | |
| 7 | Chips de asignatura (4 colores) | Texto oscuro legible sobre cada tiza | | |
| 8 | UTP: nueva unidad vacía | 4 campos con error e ícono | | |
| 9 | UTP: nueva unidad válida | Vuelve a lista con tarjeta | | |
| 10 | Enviar a revisión → aprobar | Estado cambia; historial con fechas | | |
| 11 | UTP: rechazar | Vuelve a Borrador; historial | | |
| 12 | UTP: editor de preguntas (vacío) | Errores por campo; no guarda | | |
| 13 | **Recurso nativo 1**: elegir imagen de apoyo | Selector abre; imagen se ve con borde redondeado | | |
| 14 | Cerrar y reabrir app | Unidades, imagen y estados persisten | | |
| 15 | Docente: asignar sin confirmar / sin fechas | Errores (con ícono) en fechas, intentos, checkbox | | |
| 16 | Docente: hasta < desde | Error «no puede ser anterior» | | |
| 17 | Docente: asignación válida (2° A, intentos 2, switch activo) | Aparece en «vigentes» | | |
| 18 | Estudiante `ana` (2° A) | Solo unidades publicadas, asignadas y en fecha | | |
| 19 | Estudiante: Historia | No aparece (sin asignar) | | |
| 20 | Estudiante: curso sin asignaciones | Estado vacío animado | | |
| 21 | Estudiante: leer contenido | Texto + imagen | | |
| 22 | Estudiante: rendir prueba | Una pregunta a la vez; correcta/incorrecta con ícono y animación | | |
| 23 | Estudiante: agotar intentos | Bloqueo | | |
| 24 | Informe docente | % = último intento; Historia sin asignar no cuenta | | |
| 25 | Estudiante no ve informes | Sin acceso por UI ni navegación | | |
| 26 | Observación: vacía / muy larga | Error con ícono | | |
| 27 | Observación válida | Se guarda y reaparece | | |
| 28 | **Recurso nativo 2**: «Activar avisos» | **API 29:** sin diálogo, avisos activos. **API 33+:** diálogo de permiso | | |
| 29 | Permiso concedido → guardar observación | Notificación visible | | |
| 30 | Permiso denegado → guardar observación | No falla ni se cierra la app | | |
| 31 | Reiniciar progreso (con confirmación) | % vuelve a 0; intentos no cambian | | |
| 32 | Reiniciar intentos (con confirmación) | Estudiante puede rendir de nuevo | | |
| 33 | Rotar (Panel, Login, Bienvenida, Formulario, Evaluación, Informe) | Nada cortado; texto escrito se conserva | | |
| 34 | Barras del sistema (3 botones en API 29; gestos en 33+) | Contenido no tapado | | |
| 35 | Transiciones al avanzar y volver | Deslizamiento + fundido ~300 ms | | |
| 36 | Cerrar sesión y entrar con otro rol | Cada rol ve solo lo suyo | | |

## Diferencias esperadas entre entornos
- Caso 13: en API 29 el selector puede ser el de archivos de respaldo; en 33+ el del sistema.
- Caso 28: el diálogo de permiso **solo** existe en API 33+. Es el que se demuestra en la defensa.
- Caso 34: botones de navegación vs gestos.

## Errores encontrados
| # | Caso | Descripción | Estado |
|---|---|---|---|
| | | | |
