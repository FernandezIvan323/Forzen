# Forzen — pendientes (retomar)

**Estado al guardar:** web + release v1.2.0 listos para usuarios.  
**No bloqueante:** falta sobre todo confianza de instalación y compartir.  
**Última sesión (2026-07-21):** OG banner, SHA-256 en release, enlace VT por hash.

## Alta prioridad
1. Firma de código del `.exe` (SmartScreen). — **pendiente** (certificado Authenticode / CI).
2. VirusTotal: subir `Forzen-Setup-1.2.0.exe` **una vez** y confirmar que el reporte existe.  
   - Enlace ya apunta al hash:  
     https://www.virustotal.com/gui/file/dc98e14c2c1d4fa9e1a55b77bed2882955c42549cb9617072ef695d3932bd2d2  
   - Si VT dice “not found”, subir el `.exe` desde la UI de VT (download.html tiene el botón).
3. ~~Banner **OG 1200×630** (`docs/og-banner.png`)~~ — **hecho** (meta en index / download / panel).
4. ~~Publicar **SHA-256** en el body del release de GitHub~~ — **hecho**  
   (exe `DC98E14C…D2D2`, msi `4BC09B27…283A`).

## Media
5. Capturas reales de la app en Panel de control (sustituir mocks CSS).
6. Alinear OCR: quitado de landing; sigue en app + panel (deprecar o marcar experimental).
7. CI smoke de enlaces / Pages.
8. Analytics ético opcional (o cero telemetría).

## Baja / v1.3
9. Seguimiento de foco de teclado (app).
10. Audio resumen MP3 propio.
11. Badge VT solo si confirma limpio.
12. Changelog más visible en download.

## Operativo
13. Probar instalador en PC limpio (SmartScreen, AV, menú Inicio).
14. Beta con usuarios de baja visión.
15. Decidir **1.2.1** (hotfix) vs **1.3** (foco + firma).

## Primeros pasos al retomar (ahora)
1. **Vos:** abrir [VirusTotal upload](https://www.virustotal.com/gui/home/upload), subir `Forzen-Setup-1.2.0.exe`, esperar el reporte y (si queda limpio) opcionalmente badge en download.
2. Push de esta rama / commit con `og-banner.png` + meta OG (si aún no está en `main`).
3. Probar preview de enlace en WhatsApp/Discord (caché OG a veces tarda).
4. Prueba en máquina limpia + issues.

## Enlaces útiles
- Web: https://FernandezIvan323.github.io/Forzen/
- Descarga: https://FernandezIvan323.github.io/Forzen/download.html
- Panel: https://FernandezIvan323.github.io/Forzen/panel-de-control.html
- Release: https://github.com/FernandezIvan323/Forzen/releases/tag/v1.2.0
- OG image: https://FernandezIvan323.github.io/Forzen/og-banner.png
