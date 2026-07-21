# Forzen — pendientes (retomar)

**Estado:** v1.2.0 publicado. **Web de producto cerrada** (landing, panel, download, cursors, ambient).  
**Última sesión (2026-07-21):** SEO/indexación revisados; plan de dominio guardado (issue).  
**Descartado:** capturas reales del panel (mocks CSS se quedan).

## Alta prioridad
1. Firma de código del `.exe` (SmartScreen / Authenticode). — **pendiente** (certificado).
2. VirusTotal: **subir una vez** `Forzen-Setup-1.2.0.exe` y confirmar reporte limpio.  
   - Enlace: https://www.virustotal.com/gui/file/dc98e14c2c1d4fa9e1a55b77bed2882955c42549cb9617072ef695d3932bd2d2  
3. ~~Banner OG 1200×630~~ — **hecho** (`docs/og-banner.png` + meta).
4. ~~SHA-256 en body del release~~ — **hecho**.
5. **Instalador 1.2.1:** wizard Finalizar a veces reabre 1.ª pantalla (app sí instaló).  
   - Fix en scripts: `--win-upgrade-uuid` + `--win-menu-group Forzen` (en main).  
   - Publicar tag `v1.2.1` cuando se quiera nuevo Setup.
6. **Dominio propio** — esperar compra del usuario; checklist en **[#37](https://github.com/FernandezIvan323/Forzen/issues/37)** (retomar al avisar).  
   - Home **ya indexada** en Google (`site:fernandezivan323.github.io/Forzen`).  
   - Ranking genérico «lupa Windows» aún no esperado; marca `Forzen` sí.

## Media
5. ~~Capturas reales del panel~~ — **descartado**.
6. ~~OCR experimental~~ — **hecho** (app + panel + README).
7. ~~CI Pages smoke~~ — **hecho**.
8. ~~Analytics~~ — **cero telemetría**.
9. ~~Web UI (nav, ambient, cursors)~~ — **hecho** (cerrar fase web).

## Baja / v1.3
9. Seguimiento de foco de teclado (app).
10. Audio resumen MP3 propio.
11. Badge VT solo si el reporte confirma limpio.
12. ~~Changelog en download~~ — **hecho**.
13. (Opcional SEO) página «vs Lupa de Windows» tras dominio.

## Operativo
13. Probar instalador en PC limpio (SmartScreen, AV, menú Inicio).
14. Beta con usuarios de baja visión.
15. Decidir **1.2.1** (hotfix) vs **1.3** (foco + firma).

## Al retomar
1. **Dominio:** cuando el usuario diga el nombre comprado → Pages custom domain + canónicos + sitemap + Search Console (ver issue).
2. Subir .exe a VirusTotal.
3. Tag `v1.2.1` si se quiere instalador con UUID de upgrade.
4. Issue #30 calidad de imagen de la lupa (app) — cuando se pida.

## Enlaces
- Web: https://FernandezIvan323.github.io/Forzen/
- Descarga: https://FernandezIvan323.github.io/Forzen/download.html
- Release: https://github.com/FernandezIvan323/Forzen/releases/tag/v1.2.0
- Search Console: https://search.google.com/search-console
