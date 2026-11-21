# MPAZ PRO 2°

Aplicación móvil Android de apoyo didáctico para estudiantes de Segundo Básico de la
Escuela Escritora Marcela Paz (San Bernardo). Organiza unidades con material de estudio y
evaluaciones para las cuatro asignaturas del nivel (Lenguaje, Matemática, Ciencias Naturales e
Historia), con tres perfiles: Jefatura UTP, Docente y Estudiante. La app asiste al profesor, no lo
reemplaza: **no hay diagnóstico automático por IA** (el nivel de logro es una regla fija por porcentaje).

Proyecto de la asignatura DSY1105 · Desarrollo de Aplicaciones Móviles (Evaluación 2).
**Todos los datos son ficticios.**

En esta etapa la app se **conecta a un backend** de cinco microservicios Spring Boot detrás de un API Gateway (repositorio
aparte, `mpaz-backend`) y funciona también **sin conexión**: guarda en el teléfono lo necesario para practicar y rendir la
prueba, y sincroniza solo cuando vuelve la red.

## Equipo

| Integrante | Rol en el desarrollo |
|---|---|
| Max Herrera | Login y panel por rol, informes docentes, observaciones y notificaciones locales |
| Victor Navarrete | Unidades y estados, evaluaciones, asignación a cursos y selector de fotos |

- Repositorio: https://github.com/Magiciandev/ProyectoAppMarcelaPaz

## Funcionalidades

| Perfil | Qué puede hacer |
|---|---|
| Jefatura UTP | Crear unidades (formulario validado) y llevar cada objetivo por sus 5 estados (borrador, en revisión, autorizado, publicado, deshabilitado) con su historial; editar preguntas de la prueba; elegir imagen de apoyo; ver el catálogo del servidor |
| Docente | Ver objetivos autorizados o publicados, asignarlos a un curso con fechas desde/hasta, intentos (1 a 5) y disponibilidad; ver por estudiante el avance, la última conexión y la última sincronización (con un mensaje que ayuda a saber si no avanzó o no se conectó); informes con nivel de logro por regla fija; guardar y validar la observación; reiniciar progreso e intentos con confirmación |
| Estudiante | Ver solo lo publicado y asignado a su curso y en fecha; leer contenidos; practicar actividades (4 tipos de pregunta); rendir la prueba con intentos limitados; ver su avance. **Sin conexión** puede hacer todo esto con lo ya guardado y se envía al volver la red |

Transversal: login contra el backend (JWT) con copia local del PIN cifrado (PBKDF2 con sal) para entrar sin conexión,
informes bloqueados al estudiante (por navegación y porque el servidor decide por el rol del token), barras de progreso animadas,
transiciones de navegación, estados de carga y vacío, rotación vertical/horizontal.

### Funcionamiento sin conexión (modo híbrido)
- **Se guarda en Room:** el catálogo, las asignaciones del estudiante, el paquete de evaluación de cada objetivo, el avance (ítems completados) y los intentos de prueba terminados que aún no confirma el servidor.
- **Sincronización automática** (WorkManager): al volver la red se envían primero los intentos y luego el avance. Los envíos se pueden repetir sin duplicar (cada intento y respuesta lleva un `uuid`).
- **Intentos:** si el estudiante rinde más intentos sin red que el límite, el servidor toma solo los primeros N por fecha del teléfono.
- **Si se borran los datos de la app con algo sin enviar, se pierde:** no hay respaldo mientras no hay conexión.
- La ventana de fechas se valida con la fecha del teléfono; el servidor tiene la última palabra al sincronizar.

### Recursos nativos
1. **Selector de fotos del sistema** (`PickVisualMedia`): la UTP elige la imagen de apoyo; se copia al almacenamiento interno y se muestra con Coil. No requiere permisos.
2. **Notificaciones locales**: canal propio, permiso `POST_NOTIFICATIONS` pedido en ejecución solo desde Android 13 (botón «Activar avisos»); en Android 10 no hay diálogo.

### Formularios validados (validación en `viewmodel/validation`, no en la UI)
Login · Nueva unidad · Asignar a curso (texto, entero, fecha, switch, checkbox y botón) · Editor de preguntas · Observación docente.
Cada campo muestra ícono y mensaje de error.

## Arquitectura

MVVM por capas, flujo de datos unidireccional y Repository como fuente única de la verdad.

```
App Android (Kotlin, Compose, MVVM, Room, Retrofit, WorkManager)
      |  HTTP + JSON + JWT (Authorization: Bearer ...)
      v
API Gateway :9090 -> ms-auth · ms-contenidos · ms-actividades-evaluaciones · ms-progreso · ms-admin
```

```
app/src/main/java/com/example/mpazpro/
├── MainActivity.kt          Actividad única; arma BD → cliente de red → repositorios → fábrica de ViewModels
├── ui/
│   ├── theme/               Color, tipografía y tema Material 3 (claro y oscuro)
│   ├── navigation/          Grafo de navegación (AppNavHost)
│   ├── screens/             Pantallas (sin lógica de negocio)
│   └── components/          Componentes reutilizables
├── viewmodel/               Estado con StateFlow y sealed interface
│   └── validation/          Validaciones como funciones puras (con pruebas)
├── sync/                    WorkManager: envío de lo pendiente cuando hay conexión
└── data/
    ├── model/               Entidades y reglas puras (cálculo de avance, política de intentos, mensajes)
    ├── local/               Room (base v10) y DAOs
    ├── remote/              Retrofit: APIs, DTOs, sesión (token) y resultado de cada llamada
    └── repository/          Repositorios por tema (catálogo, asignación, prueba, avance, bandeja de intentos…)
```

La lógica (cálculos, mapeos, políticas) vive en objetos puros que se prueban sin Android; las llamadas al servidor devuelven
`ResultadoRemoto` (Ok, ErrorHttp o SinConexion), así la UI nunca ve excepciones de red.

## Cómo ejecutar

1. Levantar el backend (`mpaz-backend`): MySQL encendido, `build-all.bat` y `docker compose up -d`. Detalle y direcciones según dónde corra la app (emulador, celular por cable o por WiFi) en `docs/CONEXION_BACKEND.md`.
2. Clonar este repositorio, abrirlo en Android Studio y esperar la sincronización de Gradle (JDK incluido en Android Studio).
3. Crear o elegir un emulador con Android 10 (API 29) o superior, o conectar un celular con depuración USB activada. El emulador llega al backend en `http://10.0.2.2:9090/` (valor por defecto).
4. Ejecutar el módulo `app`.
5. Pruebas unitarias: `./gradlew testDebugUnitTest` (291 pruebas) o, en Android Studio, clic derecho en `app/src/test` → Run Tests. No necesitan el backend.

Requisitos: `minSdk` 29, `targetSdk`/`compileSdk` 36, AGP 8.12.3, Kotlin 2.0.21, Room 2.6.1, Compose BOM 2024.09.00, Retrofit 2.11.0, OkHttp 4.12.0, WorkManager 2.9.1, Coil 2.7.0.
Sin backend la app abre y deja entrar con la copia local, pero no hay catálogo nuevo ni envío de datos.

### Usuarios de prueba (ficticios)
`utp`, `docente`, `ana`, `beto`, `carla`, `gael` (2° A), `diego`, `eva`, `isidora`, `felipe` (2° B). PIN de todos: `1234`.
El PIN se guarda cifrado, nunca en claro. Si la base ya existía con datos viejos, desinstalar la app para recrearla.
El objetivo LEN-OA01 está publicado con 4 preguntas (los 4 tipos), 4 actividades y su prueba final; `ana` lo tiene completo y `beto` no se conecta hace 10 días (sirve para ver el mensaje de inactividad).

### Recorrido sugerido
Entrar como `utp` → «Catálogo del servidor» → crear unidad → enviar a revisión → autorizar → publicar → editar preguntas.
Entrar como `docente` → asignar un objetivo publicado a un curso → «Informes de estudiantes» → avance, última conexión y sincronización → observación → validar.
Entrar como `ana` → ver solo lo asignado a 2° A → leer contenido → practicar actividades → rendir la prueba.
Sin conexión: abrir antes la lista, las actividades y la prueba con red; activar modo avión; rendir de nuevo y ver «Tu intento quedó guardado en este teléfono»; volver la red y comprobar en el docente que llegó.

## Convenciones de trabajo
- **Commits:** prefijo `feat:`, `fix:`, `refactor:`, `test:` o `docs:` + frase técnica en infinitivo.
- **Ramas:** una por funcionalidad (`feature/nombre`), unida a `master` con pull request revisado por el otro integrante.
- **Revisión:** lista de la plantilla `.github/pull_request_template.md`.

## Decisiones técnicas
- **minSdk 29:** heredado del ejemplo del profesor; instala en más equipos. Lo que depende de Android 13+ está protegido por versión.
- **Fábrica de ViewModels manual:** sin Hilt/Koin; el ViewModel nunca recibe `Context` (lo que lo necesita, como WorkManager, entra como función desde `MainActivity`).
- **Un solo punto de entrada al backend:** el gateway; el JWT se guarda en el teléfono y se agrega a cada llamada con un interceptor.
- **Room con `fallbackToDestructiveMigration`:** solo desarrollo (datos ficticios). Si cambia el esquema se sube `version` en `AppDatabase`.
- **Modo híbrido sin conexión:** caché en Room + cola de intentos pendientes + envío con WorkManager (primero intentos, luego avance). El servidor decide: gana el avance más reciente y de los intentos valen los primeros N.
- **El servidor no calcula el avance:** la app lo calcula (contenidos + actividades + desafíos completados sobre el total, sin la prueba) y lo envía.
- **Ids locales ≠ ids del servidor:** todo lo local del estudiante (avance, intentos pendientes) usa el id de Room.
- **Sin color dinámico:** la app conserva su paleta «pizarra y tizas», con contraste WCAG AA.
- **Restricciones del caso:** sin IA generativa, sin micrófono y sin datos reales de estudiantes.

## Documentación
`docs/QA.md` (matriz de pruebas API 29 / API 33+) · `docs/DEFENSA.md` (guion de 15 minutos) · `docs/ENTREGA.md` (lista de entrega) · `docs/CONEXION_BACKEND.md` (conexión con el backend).
