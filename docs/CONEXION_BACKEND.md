# Conexión de la app con el backend (fase 5)

La app habla con el backend a través de **un solo punto de entrada: el API Gateway** (puerto 9090). Esta nota explica
cómo apuntarla al backend y cómo está armada la capa remota. El diseño completo está en el documento
«Contrato de la API y modelo de datos v2».

## 1. Antes de probar

1. Levantar el backend (`mpaz-backend`): MySQL encendido, `build-all.bat` y `docker compose up -d`.
2. Verificar que responde: abrir `http://localhost:9090/auth/login` en el navegador debe dar un error 405 o 401 (no
   «no se puede conectar»).

## 2. Dirección del backend según dónde corra la app

| Dónde corre la app | URL base | Cómo se configura |
| --- | --- | --- |
| Emulador de Android Studio | `http://10.0.2.2:9090/` | Es el valor por defecto; no hay que hacer nada |
| Celular por cable | `http://localhost:9090/` | `adb reverse tcp:9090 tcp:9090` y compilar con `-PapiBaseUrl=http://localhost:9090/` |
| Celular por WiFi | `http://IP-DEL-PC:9090/` | Compilar con `-PapiBaseUrl=http://IP-DEL-PC:9090/` (el PC y el celular en la misma red) |

`-PapiBaseUrl=...` se puede poner en la línea de Gradle o en `~/.gradle/gradle.properties` como `apiBaseUrl=...`.
La URL queda en `BuildConfig.API_BASE_URL` y la lee `ApiConfig`.

El tráfico HTTP sin cifrar solo se permite en la variante **debug** (`app/src/debug/AndroidManifest.xml`). La variante
release no lo permite: en producción habría que usar HTTPS.

## 3. Cómo está armada la capa remota (`data/remote`)

| Pieza | Para qué sirve |
| --- | --- |
| `ApiConfig` | La URL base. |
| `SesionStore` y `AlmacenClaves` | Guardan el token del servidor en SharedPreferences (sin cifrar: limitación del MVP). |
| `AuthInterceptor` | Agrega `Authorization: Bearer <token>` a cada petición, salvo el login. |
| `ClienteApi` | Un solo `OkHttpClient` y un solo Retrofit con Gson; entrega `auth`, `contenidos`, `evaluaciones`, `progreso` y `admin`. |
| `api/` | Una interfaz de Retrofit por servicio, con todos los endpoints del contrato. |
| `dto/` | La forma del JSON (clases `...Dto`). Las fechas viajan como texto ISO-8601 UTC. |
| `ResultadoRemoto` y `llamarApi` | Convierten los fallos en `Ok`, `ErrorHttp` (con mensaje listo para mostrar) o `SinConexion`. |
| `AutenticacionRemota` | Inicia y cierra la sesión con el servidor. |

Regla de uso: **las pantallas y los ViewModels no llaman a Retrofit**. Lo hace un repositorio, que además guarda en
Room lo que necesita para funcionar sin conexión (ejemplo: `CatalogoRepository`).

## 4. Qué cambió en esta fase

- **Login:** `UsuarioRepository.autenticar` valida primero en el servidor. Si no hay conexión (o el servidor falla con
  un 5xx) valida contra la copia local con el último PIN que se usó con conexión. Un 401 es siempre «Usuario o PIN
  incorrectos». El usuario local sigue siendo el que usan las demás pantallas.
- **Cerrar sesión** olvida el token.
- **Room v5:** nuevas tablas `cache_asignatura`, `cache_unidad`, `cache_objetivo` y `cache_contenido` con el catálogo
  descargado. Al actualizar la app, **la base local se recrea** (política de desarrollo `fallbackToDestructiveMigration`);
  los usuarios de prueba se vuelven a sembrar solos.
- **Pantalla «Catálogo del servidor»** (botón en el panel de UTP y docente; el estudiante no lo ve): muestra lo guardado y lo actualiza desde
  el backend. Cada perfil ve lo que su rol puede ver: UTP todo; docente lo autorizado y publicado; estudiante lo
  publicado.

Fase 6, paso 4a: **el estudiante** ve en «Mis evaluaciones» lo que su docente le asignó en el servidor (con los intentos
usados y permitidos y la fecha límite) y puede leer el contenido de esa unidad. Al abrir la pantalla también registra su
última conexión. La prueba de esas unidades llega en el paso 4b.

Fase 6, paso 3: **asignar una unidad a un curso** (docente) se hace en el servidor cuando la unidad existe allí (crea
la asignación o, si ya existía para ese curso, la edita). Las asignaciones del servidor no se copian a la base local:
los estudiantes las verán cuando su flujo pase al servidor (paso 4). Las unidades de ejemplo siguen asignándose en el
teléfono.

Fase 6, paso 2: **los estados de la UTP** (enviar a revisión, autorizar, rechazar, publicar, deshabilitar y reabrir) se
piden a `ms-admin` cuando la unidad existe en el servidor, y en «Unidades» la UTP trae las del servidor (al abrir la
pantalla y con el botón «Actualizar»). El servidor puede rechazar el cambio (por ejemplo, enviar a revisión exige una
prueba final con al menos una pregunta): el motivo se muestra en el detalle de la unidad.

Fase 6, paso 1: **crear una unidad** ahora se hace en el servidor (unidad, objetivo y contenido) y solo después se guarda
la copia local; hace falta conexión y ser UTP. Lo demás de la app (estados, evaluación, asignación e informes)
**sigue funcionando con la base local**: se migra en los pasos siguientes de la fase 6 y en la fase 7.

## 5. Pruebas manuales sugeridas

1. Con el backend arriba, ingresar como `utp` / `1234`: en el panel, «Catálogo del servidor» debe mostrar las 4 asignaturas
   con sus unidades en distintos estados (PUBLICADO, AUTORIZADO, EN_REVISION, BORRADOR).
2. Cerrar sesión e ingresar como `docente`: ya no aparecen los objetivos en BORRADOR ni EN_REVISION.
3. Ingresar como `ana`: solo lo PUBLICADO.
4. Apagar el backend (`docker compose stop`) y abrir el catálogo: se ve lo guardado, con el aviso de que no hay conexión.
5. Con el backend apagado, ingresar como `ana` / `1234`: entra igual (valida contra la copia local).
6. PIN incorrecto con el backend arriba: «Usuario o PIN incorrectos».
