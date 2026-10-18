# Guion de defensa — 15 minutos (individual)

**Antes:** app instalada y probada en tu equipo; copia **local** del proyecto para ensayar (jamás subir cambios: nota 1.0). Abrir Android Studio con el proyecto ya sincronizado. Saber: usuarios `utp`/`docente`/`ana`, PIN `1234`.
Si la app no corre, lo demás vale 0: ensayar el arranque desde cero.

| Min | Paso | Qué mostrar / decir | Dónde está |
|---|---|---|---|
| 1:00 | 1. Entorno | Abrir y ejecutar; entrar como `utp` | `MainActivity.kt` |
| 2:00 | 2. Arquitectura | Flujo: Screen → ViewModel → Repository → DAO → Room. El estado baja, los eventos suben. ViewModel sin Context; MainActivity arma la cadena | `viewmodel/MpazViewModelFactory.kt`, `data/repository/`, `data/local/` |
| 1:30 | 3. Diseño | Paleta pizarra/tizas, texto grande, contraste AA, claro/oscuro, `Surface` base | `ui/theme/Color.kt`, `Theme.kt`, `MainActivity.kt` |
| 2:00 | 4. Formularios | Enviar vacío → errores con ícono; el validador es una función pura con pruebas | `viewmodel/validation/*`, `ui/components/CampoValidado.kt`, `MensajeError.kt` |
| 1:30 | 5. Estado | `MutableStateFlow` privado → `StateFlow` público; `sealed interface` Cargando/Listo/Error; `collectAsStateWithLifecycle` | cualquier `*ViewModel.kt` |
| 1:00 | 6. Animaciones | Bienvenida, transiciones de navegación, opción correcta/incorrecta, barras del informe, estado vacío | `AppNavHost.kt`, `OpcionRespuesta.kt`, `InformeScreen.kt`, `EstadosVista.kt` |
| 2:00 | 7. Room | Entidades, DAO con Flow/suspend, `version = 4`, repositorio con `Dispatchers.IO` y `Result`; cerrar/reabrir y persiste; consulta con AVG/GROUP BY | `data/local/AppDatabase.kt`, `InformeDao.kt` |
| 1:30 | 8. Recursos nativos | Selector de fotos (sin permiso) y notificación con permiso desde API 33 (`areNotificationsEnabled` + `POST_NOTIFICATIONS`); resultado visible en la UI | `UnidadDetalleScreen.kt`, `ImagenApoyo.kt`, `Notificaciones.kt`, `InformeScreen.kt` |
| 2:30 | 9. Modificación en vivo | Un ejercicio de la lista (abajo) | — |
| **15:00** | | | |

Peso de la nota: estructura modular 22 %, validación 20 %, persistencia 18 %, recurso nativo 12 %, resto 7 % c/u. El 72 % es modificar o ejecutar en vivo.

## Ejercicios cronometrados (< 3 min, sin ayuda)

Ensayar cada uno con cronómetro; marcar tu mejor tiempo.

| # | Ejercicio | Ruta de cambios | Tiempo |
|---|---|---|---|
| 1 | **Agregar una regla a una validación** (ej. título sin números) | `viewmodel/validation/ValidadorUnidad.kt` (nueva condición en el `when`) → agregar caso en `ValidadorUnidadTest.kt` → Run test | |
| 2 | **Agregar un campo a un formulario, de punta a punta** (ej. «Duración» en la unidad) | `data/model/Unidad.kt` (campo) → `AppDatabase.kt` (subir `version`) → `UnidadFormViewModel.kt` (estado + `actualizar…`) → `ValidadorUnidad.kt` → `UnidadFormScreen.kt` (`CampoValidado`) → `strings.xml` | |
| 3 | **Crear un Composable nuevo con su estado** (ej. contador o botón «favorito») | Archivo nuevo en `ui/components/`; estado con `remember { mutableStateOf }` o elevado al ViewModel; llamarlo desde una pantalla | |
| 4 | **Agregar una animación** (ej. `AnimatedVisibility` o `animateColorAsState`) | Envolver un elemento en `ui/screens/…` con `AnimatedVisibility(visible = …)`; o copiar el patrón de `OpcionRespuesta.kt` | |
| 5 | **Consulta nueva en el DAO y mostrarla** (ej. contar unidades por estado) | `UnidadDao.kt` (`@Query` + `Flow`/`suspend`) → `UnidadRepository.kt` → `UnidadesViewModel.kt` (StateFlow) → `UnidadesScreen.kt` (Text) | |
| 6 | **Cambiar la presentación de la imagen** (borde, tamaño, forma) | `ui/components/ImagenApoyo.kt`: `RoundedCornerShape`, `height`, `border`, `ContentScale` | |
| 7 | **Cambiar el flujo del permiso de la notificación** (ej. pedir el permiso al abrir el informe, o mostrar explicación antes) | `ui/components/Notificaciones.kt` + lanzador en `InformeScreen.kt` (botón «Activar avisos») | |

## Preguntas típicas (respuesta corta)
- **¿Por qué el ViewModel no usa Context?** Porque sobrevive a la rotación y un Context filtraría la Activity; el Context entra solo por MainActivity y la capa UI.
- **¿Dónde está la validación y por qué ahí?** En `viewmodel/validation` como funciones puras: se prueban sin Android y la UI solo muestra el resultado.
- **¿Cómo se cifra la clave?** PBKDF2 con sal (`data/security/Cifrado.kt`); se guarda el hash, nunca el PIN.
- **¿Qué pasa si cambio el esquema de Room?** Subo `version`; con `fallbackToDestructiveMigration` se recrea (solo desarrollo).
- **¿Cómo evitas que el estudiante vea informes?** El repositorio exige rol Docente en cada operación, además de la navegación.
- **¿Por qué no hay permiso para el selector de fotos?** El selector del sistema entrega solo la imagen elegida; no hace falta permiso.
