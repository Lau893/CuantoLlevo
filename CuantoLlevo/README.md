# Cuánto llevo (Android, Java)

App Android escrita en Java puro, sin librerías externas: solo el SDK de Android. Toda la interfaz se arma desde el código, sin layouts XML. Cada cuenta empieza a correr al crearla, tiene su propio color y cuenta cuántas veces la reiniciaste. La interfaz es blanco, negro y grises; el único color lo ponen las cuentas. Con el botón de arriba a la derecha se elige el modo: Sistema (sigue al teléfono), Claro u Oscuro.

## Cómo correrla

1. Abrí esta carpeta en Android Studio (**File → Open**).
2. **File → Sync Project with Gradle Files** y esperá a que termine.
3. Elegí el emulador o tu celular y tocá **Run ▶**.

Requiere Android 8.0 (API 26) o superior.

## Generar el APK e instalarlo en el celular

Desde esta carpeta, con un JDK 17 o 21 (por ejemplo `C:\Users\Lautaro\.jdks\jbr-21.0.11`; el JDK 25 que trae Android Studio es demasiado nuevo para Gradle 8.7):

```
set JAVA_HOME=C:\Users\Lautaro\.jdks\jbr-21.0.11
gradlew assembleDebug
```

El APK queda en `app\build\outputs\apk\debug\app-debug.apk`. Pasalo al celular (cable, Drive, WhatsApp, etc.), abrilo y aceptá "Instalar apps de origen desconocido" cuando lo pida.

## Pantallas (una Activity cada una)

- `MainActivity`: la lista de cuentas con el contador en vivo, el modo "Ordenar" (↑ ↓) y el botón "+ Nueva cuenta".
- `NuevaCuentaActivity`: nombre, color y, opcionalmente, fecha y hora de inicio.
- `DetalleActivity`: desglose compacto, reinicios, totales, editar nombre, cambio de color, detener/retomar, reiniciar (a cero o desde una fecha) y borrar.

## Resto del código

- `Cuenta`: el modelo de una cuenta.
- `CuentasStore`: guarda las cuentas en el teléfono (SharedPreferences, JSON).
- `Tiempo`: cálculo de años/meses/días/horas y formato de textos.
- `ColorCuenta`: los 8 colores de cuenta (tono claro y oscuro).
- `Paleta`: los grises de la interfaz según el modo claro u oscuro.
- `Ui`: piezas de interfaz compartidas (botones, textos, puntos de color, fondo).
- `SelectorColor`: los círculos para elegir color.
