# Forzen

**Lupa de sistema inteligente para personas con baja visión.**

Amplía cualquier área de tu pantalla al instante. Hecho en Java, para Windows 10/11.

[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-00FF41)](https://adoptium.net/)
[![Build](https://github.com/FernandezIvan323/Forzen/actions/workflows/release.yml/badge.svg)](https://github.com/FernandezIvan323/Forzen/actions)

🌐 [Página web](https://FernandezIvan323.github.io/Forzen/) · 📦 [Descargar instalador](https://github.com/FernandezIvan323/Forzen/releases/latest)

---

## Para usuarios (sin comandos)

1. Entra a [Releases](https://github.com/FernandezIvan323/Forzen/releases/latest) o a la [web](https://FernandezIvan323.github.io/Forzen/#download).
2. Descarga **Forzen-Setup-….exe**.
3. Doble clic → asistente de Windows (Siguiente / Instalar).
4. Si pide permisos, pulsa **Sí**.
5. Si SmartScreen avisa: **Más información** → **Ejecutar de todos modos**.
6. Abre Forzen desde el menú Inicio. Icono en la bandeja del sistema.

No necesitas instalar Java ni usar la terminal.

Desinstalar: **Configuración de Windows → Aplicaciones → Forzen**.

---

## Funcionalidades

| | |
|---|---|
| 🔍 | Zoom 1x–8x (modo Lens) |
| 🖥️ | Modos **Lens**, **Full-Screen** y **Docked** |
| ⚡ | Captura GDI + exclusión de la propia ventana |
| 🖱️ | Click-through en Lens/Docked |
| 🖥️ | Multi-monitor + corrección DPI |
| 🎨 | Filtros de color + brillo/contraste/saturación |
| ⌨️ | Atajos globales personalizables |
| 📝 | OCR opcional + TTS (Windows Speech) |
| 🚀 | Iniciar con Windows |
| 📊 | FPS opcional |

---

## Atajos por defecto

| Acción | Teclas |
|---|---|
| Zoom + | `Ctrl + Alt + ↑` |
| Zoom - | `Ctrl + Alt + ↓` |
| Pausar / Reanudar | `Ctrl + Alt + Z` |
| Cambiar modo | `Ctrl + Alt + M` |
| Abrir ajustes | `Ctrl + Alt + ,` |
| OCR | `Ctrl + Alt + T` |
| Salir | `Ctrl + Alt + X` |

---

## Para desarrolladores

### Requisitos

- JDK 21 con JavaFX (recomendado: **Liberica Full** / `jdk+fx`)
- Maven 3.9+
- Windows 10/11 para probar la lupa y el instalador

### Ejecutar en desarrollo

```powershell
cd Forzen
mvn test
mvn javafx:run
```

### Generar instalador Windows (local)

```powershell
.\scripts\build-installer.ps1
```

Salida en `target\installer\`:

- `Forzen-Setup-<versión>.exe` — principal (asistente)
- `Forzen-Setup-<versión>.msi` — opcional

### Publicar un release

1. `git tag v1.1.0`
2. `git push origin v1.1.0`
3. GitHub Actions genera EXE + MSI y los sube a **Releases**
4. La web apunta a `/releases/latest`

---

## Licencia

MIT — ver [LICENSE](LICENSE).
