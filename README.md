# Lemac DataLab

App Android (Kotlin + Jetpack Compose, Material 3) para autorreporte personal.

- **Autocuidado:** historial de autorreportes por ámbito (Neurodesarrollo, Habilidades DBT, Estado de Ánimo, Adicciones), con filtro, detalle, alta y baja (con confirmación).

> Herramienta académica de autorreporte personal. No emite diagnósticos ni reemplaza atención profesional o urgencias.

## Arquitectura

```
app/src/main/java/com/example/lemacdatalab/
├── data/
│   ├── model/        Modelos (RegistroAutoReporte, Ambitos)
│   ├── local/        JsonCodec (JSON ↔ modelos), LocalPreferencesStorage, SeedData
│   └── repository/   Interfaces + implementación SharedPreferences + implementación en memoria
├── ui/
│   ├── viewmodel/    RegistroViewModel y su UiState
│   ├── screens/      Lista / Formulario / Detalle de autocuidado
│   ├── components/   Componentes compartidos (EtiquetaChip, formato de fecha)
│   ├── navigation/   NavHost y rutas
│   └── theme/
└── MainActivity.kt
```

MVVM con estado inmutable (`StateFlow`). Los ViewModels dependen de una interfaz de repositorio, por lo que
los tests usan la versión en memoria sin necesitar Android.

## Reglas de validación

- Autorreporte: el tipo de actividad es obligatorio; en **Habilidades DBT** y **Adicciones** también es obligatorio el desencadenante/estrategia.

## Persistencia y privacidad

- Los datos se guardan localmente en SharedPreferences (JSON). Las fechas se almacenan como epoch (ms).
- Primera ejecución: se cargan datos de ejemplo. Si el usuario borra todo, la lista queda vacía.
- El backup automático está desactivado (`allowBackup=false`) y el archivo de preferencias está excluido de las reglas de respaldo.

## Ejecutar

Abrir la carpeta en Android Studio y ejecutar la configuración `app`, o:

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```
