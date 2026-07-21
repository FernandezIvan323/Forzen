# Forzen — pendientes (retomar)

**Estado:** v1.2.0 publicado. Confianza + docs en curso.  
**Última sesión (2026-07-21):** OG, SHA-256 en release, OCR experimental, CI Pages smoke, changelog en download.  
**Descartado:** capturas reales del panel (mocks CSS se quedan).

## Alta prioridad
1. Firma de código del `.exe` (SmartScreen / Authenticode). — **pendiente** (certificado).
2. VirusTotal: **subir una vez** `Forzen-Setup-1.2.0.exe` y confirmar reporte limpio.  
   - Enlace: https://www.virustotal.com/gui/file/dc98e14c2c1d4fa9e1a55b77bed2882955c42549cb9617072ef695d3932bd2d2  
3. ~~Banner OG 1200×630~~ — **hecho** (`docs/og-banner.png` + meta).
4. ~~SHA-256 en body del release~~ — **hecho**.
5. **Instalador 1.2.1:** al pulsar Finalizar a veces reaparece la 1.ª pantalla del wizard (app sí instaló).  
   - Fix en scripts: `--win-upgrade-uuid` estable + `--win-menu-group Forzen` (menú ya no en “Unknown”).  
   - Publicar tag `v1.2.1` cuando se quiera nuevo Setup.

## Media
5. ~~Capturas reales del panel~~ — **descartado** (decisión del usuario).
6. ~~Alinear OCR experimental~~ — **hecho** (app UI + panel + README; landing sin OCR).
7. ~~CI smoke de enlaces / Pages~~ — **hecho** (`.github/workflows/pages-smoke.yml`).
8. ~~Analytics~~ — **decisión: cero telemetría** (app + web; sin analytics de terceros).

## Baja / v1.3
9. Seguimiento de foco de teclado (app).
10. Audio resumen MP3 propio.
11. Badge VT solo si el reporte confirma limpio.
12. ~~Changelog más visible en download~~ — **hecho** (bloque hero + `#novedades`).

## Operativo
13. Probar instalador en PC limpio (SmartScreen, AV, menú Inicio).
14. Beta con usuarios de baja visión.
15. Decidir **1.2.1** (hotfix) vs **1.3** (foco + firma).

## Al retomar
1. Vos: subir .exe a VirusTotal.
2. Merge del PR de confianza/docs si aún no está en `main`.
3. Preview de enlace en WhatsApp/Discord (caché OG).
4. PC limpio + issues.

## Enlaces
- Web: https://FernandezIvan323.github.io/Forzen/
- Descarga: https://FernandezIvan323.github.io/Forzen/download.html
- Release: https://github.com/FernandezIvan323/Forzen/releases/tag/v1.2.0
