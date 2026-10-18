## Qué cambia
<!-- Una o dos líneas: qué se agregó o corrigió y en qué fase del plan. -->

## Revisión cruzada (marcar antes de aprobar)
- [ ] Ningún Composable llama a un repositorio o DAO.
- [ ] El ViewModel no tiene `Context`, `Activity` ni `View`.
- [ ] El estado se expone como `StateFlow` inmutable y solo se modifica desde el ViewModel.
- [ ] Los estados de pantalla usan `sealed interface` (Idle, Loading, Success, Error).
- [ ] El repositorio expone `Flow` o funciones `suspend`, devuelve `Result` en escrituras y usa `Dispatchers.IO` explícito.
- [ ] Cada clase está en su paquete (`ui/screens`, `ui/components`, `ui/navigation`, `viewmodel`, `data/model`, `data/local`, `data/repository`).
- [ ] Se prefiere `val` sobre `var` y los modelos son `data class` inmutables.
- [ ] La validación está fuera del Composable y tiene su prueba unitaria.
- [ ] El revisor puede explicar este cambio sin mirar el código.
