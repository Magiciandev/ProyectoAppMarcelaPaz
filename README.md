# MPAZ PRO 2°

Aplicación móvil Android de apoyo didáctico para estudiantes de Segundo Básico de la
Escuela Escritora Marcela Paz (San Bernardo). Organiza unidades con material de estudio y
evaluaciones para las cuatro asignaturas del nivel (Lenguaje, Matemática, Ciencias Naturales
e Historia), con tres perfiles: Jefatura UTP, Docente y Estudiante.

Proyecto de la asignatura DSY1105 · Desarrollo de Aplicaciones Móviles.
Todos los datos de la app son ficticios.

## Equipo

| Integrante | Rol en el desarrollo |
|---|---|
| [Nombre Apellido] | [por completar] |
| [Nombre Apellido] | [por completar] |

- Equipo N°: [por completar]
- Planificación (Trello): [enlace por completar]

## Funcionalidades

| Funcionalidad | Estado |
|---|---|
| Esqueleto del proyecto y pantalla de bienvenida | Implementado |
| Navegación entre pantallas (Navigation Compose) | Implementado |
| Formularios validados con íconos y mensajes | En curso (formulario de nueva unidad listo) |
| Persistencia local con Room | En curso (unidades guardadas y listadas) |
| Flujo UTP (unidades y pruebas) | Pendiente |
| Flujo Docente (asignación, fechas, intentos) | Pendiente |
| Flujo Estudiante (contenido y evaluación) | Pendiente |
| Informes de progreso | Pendiente |
| Animaciones | En curso (entrada de la bienvenida) |
| Recursos nativos (selector de fotos y notificaciones) | Pendiente |

## Arquitectura

MVVM por capas con flujo de datos unidireccional y Repository como fuente única de la verdad.

```
app/src/main/java/com/example/mpazpro/
├── MainActivity.kt          Actividad única
├── ui/
│   ├── theme/               Color, tipografía y tema Material 3
│   ├── navigation/          Grafo de navegación (AppNavHost)
│   ├── screens/             Pantallas completas (sin lógica de negocio)
│   └── components/          Componentes reutilizables sin estado
├── viewmodel/               Estado de la UI con StateFlow
│   └── validation/          Validaciones como funciones puras (se prueban sin Android)
└── data/
    ├── model/               Modelos y entidades
    ├── local/               Base de datos Room y DAOs
    └── repository/          Repositorios
```

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle
   (usa el JDK incluido en Android Studio).
3. Crear o elegir un emulador con Android 10 (API 29) o superior, o conectar un dispositivo.
4. Ejecutar el módulo `app`.

Requisitos: `minSdk` 29, `compileSdk` 36, AGP 8.12.3, Kotlin 2.0.21, Room 2.6.1.

## Convenciones de trabajo

- **Commits:** mensajes técnicos en infinitivo con prefijo: `feat:`, `fix:`, `refactor:`,
  `test:`, `docs:`. Ejemplo: `feat: agregar validación de título en formulario de unidad`.
- **Ramas:** una por funcionalidad (`feature/nombre`), unida a `main` mediante pull request.
- **Revisión cruzada:** cada cambio lo revisa el otro integrante con la lista de la plantilla de
  pull request (`.github/pull_request_template.md`).

## Decisiones técnicas

- **minSdk 29 (Android 10):** instala en más dispositivos y no bloquea ningún requisito. Las partes
  que dependen de Android 13+ (permiso de notificaciones) se protegen con verificación de versión.
- **Fábrica de ViewModels manual:** sin librerías de inyección; el repositorio llega por el
  constructor y el ViewModel nunca recibe un `Context`.
- **Room con `fallbackToDestructiveMigration`:** solo para desarrollo, porque todos los datos son
  ficticios. Si cambia el esquema, se sube `version` en `AppDatabase` y la base se recrea.
- **Validaciones como funciones puras** en `viewmodel/validation`, con pruebas unitarias.

## Pendiente de confirmar con el profesor

La guía de arquitectura no menciona estas carpetas, que se agregaron por necesidad:
`data/local` (Room, espejo de `data/remote`), `viewmodel/validation` y `ui/navigation`.
