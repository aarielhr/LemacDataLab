# Lemac DataLab

App Android (Kotlin + Jetpack Compose, Material 3)

> Herramienta académica de autorreporte personal. No emite diagnósticos ni reemplaza atención profesional o urgencias.

## Arquitectura

```
app/src/main/java/com/example/lemacdatalab/
├── data/
│   ├── model/        Modelos (RegistroAutoReporte, Actividad, Ambitos)
│   ├── local/        JsonCodec (JSON ↔ modelos), LocalPreferencesStorage, SeedData
│   ├── remote/       API Retrofit para GET /actividades
│   └── repository/   Repositorios local/en memoria y remoto
├── utils/            LocationHelper (Play Services FusedLocationProviderClient)
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

## Reglas de validación y funciones

- Autorreporte: el tipo de actividad es obligatorio; en **Habilidades DBT** y **Adicciones** también es obligatorio el desencadenante/estrategia.
- **Ubicación GPS:** se solicita permiso en tiempo de ejecución al presionar "Obtener GPS" y se registran las coordenadas latitud/longitud junto al autorreporte.

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

La API devuelve un arreglo JSON de actividades con el campo `nombre`, por ejemplo
`[{"nombre":"Pausa de autorregulación"}]`. La URL base predeterminada apunta al
puerto 8080 del equipo anfitrión desde el emulador Android (`10.0.2.2`); puede
sobrescribirse con `-PAPI_BASE_URL=https://servidor/`.
