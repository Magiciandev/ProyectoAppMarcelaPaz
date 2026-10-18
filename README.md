# MPAZ PRO 2°

Aplicación móvil Android de apoyo didáctico para estudiantes de Segundo Básico de la
Escuela Escritora Marcela Paz (San Bernardo). Organiza unidades con material de estudio y
evaluaciones para las cuatro asignaturas del nivel (Lenguaje, Matemática, Ciencias Naturales e
Historia), con tres perfiles: Jefatura UTP, Docente y Estudiante. La app asiste al profesor, no lo
reemplaza: **no hay diagnóstico automático por IA** (el nivel de logro es una regla fija por porcentaje).

Proyecto de la asignatura DSY1105 · Desarrollo de Aplicaciones Móviles (Evaluación 2).
**Todos los datos son ficticios.**

## Equipo

| Integrante | Rol en el desarrollo |
|---|---|
| [Nombre Apellido] | [por completar] |
| [Nombre Apellido] | [por completar] |

- Equipo N°: [por completar]
- Planificación (Trello): [pegar enlace del tablero]

## Funcionalidades

| Perfil | Qué puede hacer |
|---|---|
| Jefatura UTP | Crear unidades (formulario validado), enviarlas a revisión, aprobar/rechazar/deshabilitar, ver historial, editar preguntas de la prueba, elegir imagen de apoyo |
| Docente | Ver unidades publicadas, asignarlas a un curso con fechas desde/hasta, intentos (1 a 5) y disponibilidad; ver informes por estudiante; guardar observaciones; reiniciar progreso e intentos con confirmación |
| Estudiante | Ver solo unidades publicadas, asignadas a su curso y en fecha; leer el contenido con imagen; rendir la prueba (una pregunta a la vez, con intentos); ver su avance |

Transversal: login con PIN cifrado (PBKDF2 con sal), informes bloqueados al estudiante (por navegación y por lógica),
barras de progreso animadas, transiciones de navegación, estados de carga y vacío, rotación vertical/horizontal.

### Recursos nativos
1. **Selector de fotos del sistema** (`PickVisualMedia`): la UTP elige la imagen de apoyo; se copia al almacenamiento interno y se muestra con Coil. No requiere permisos.
2. **Notificaciones locales**: canal propio, permiso `POST_NOTIFICATIONS` pedido en ejecución solo desde Android 13 (botón «Activar avisos»); en Android 10 no hay diálogo.

### Formularios validados (validación en `viewmodel/validation`, no en la UI)
Login · Nueva unidad · Asignar a curso (texto, entero, fecha, switch, checkbox y botón) · Editor de preguntas · Observación docente.
Cada campo muestra ícono y mensaje de error.

## Arquitectura

MVVM por capas, flujo de datos unidireccional y Repository como fuente única de la verdad.

```
app/src/main/java/com/example/mpazpro/
├── MainActivity.kt          Actividad única; arma BD → repositorios → fábrica de ViewModels
├── ui/
│   ├── theme/               Color, tipografía y tema Material 3 (claro y oscuro)
│   ├── navigation/          Grafo de navegación (AppNavHost)
│   ├── screens/             Pantallas (sin lógica de negocio)
│   └── components/          Componentes reutilizables
├── viewmodel/               Estado con StateFlow y sealed interface
│   └── validation/          Validaciones como funciones puras (con pruebas)
└── data/
    ├── model/               Entidades y reglas puras
    ├── local/               Room (base v4) y DAOs
    └── repository/          Repositorios (Flow/suspend, Result, Dispatchers.IO)
```

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle (JDK incluido en Android Studio).
3. Crear o elegir un emulador con Android 10 (API 29) o superior, o conectar un celular con depuración USB activada.
4. Ejecutar el módulo `app`.
5. Pruebas unitarias: `./gradlew testDebugUnitTest` (70 pruebas) o, en Android Studio, clic derecho en `app/src/test` → Run Tests.

Requisitos: `minSdk` 29, `targetSdk`/`compileSdk` 36, AGP 8.12.3, Kotlin 2.0.21, Room 2.6.1, Compose BOM 2024.09.00.

### Usuarios de prueba (ficticios)
`utp`, `docente`, `ana`, `beto`, `carla` (2° A), `diego`, `eva`, `felipe` (2° B). PIN de todos: `1234`.
El PIN se guarda cifrado, nunca en claro. Si la base ya existía con datos viejos, desinstalar la app para recrearla.

### Recorrido sugerido
Entrar como `utp` → crear unidad → enviar a revisión → aprobar → elegir imagen de apoyo → editar preguntas.
Entrar como `docente` → asignar la unidad a un curso → (como estudiante rendir la prueba) → «Informes de estudiantes» → observación → reiniciar.
Entrar como `ana` → ver solo unidades asignadas a 2° A → leer contenido → rendir prueba.
Historia queda sin asignar a propósito: ningún estudiante la ve.

## Convenciones de trabajo
- **Commits:** prefijo `feat:`, `fix:`, `refactor:`, `test:` o `docs:` + frase técnica en infinitivo.
- **Ramas:** una por funcionalidad (`feature/nombre`), unida a `main` con pull request revisado por el otro integrante.
- **Revisión:** lista de la plantilla `.github/pull_request_template.md`.

## Decisiones técnicas
- **minSdk 29:** heredado del ejemplo del profesor; instala en más equipos. Lo que depende de Android 13+ está protegido por versión.
- **Fábrica de ViewModels manual:** sin Hilt/Koin; el ViewModel nunca recibe `Context`.
- **Room con `fallbackToDestructiveMigration`:** solo desarrollo (datos ficticios). Si cambia el esquema se sube `version` en `AppDatabase`.
- **Intentos por unidad** (clave `intentos_max_<unidadId>`, defecto 3), configurados por el docente.
- **Sin color dinámico:** la app conserva su paleta «pizarra y tizas», con contraste WCAG AA.
- Sin backend ni Retrofit: el MVP usa solo base local.

## Documentación
`docs/QA.md` (matriz de pruebas API 29 / API 33+) · `docs/DEFENSA.md` (guion de 15 minutos) · `docs/ENTREGA.md` (lista de entrega).
