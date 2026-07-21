(function () {
  'use strict';

  function initLiveStage() {
    var host = document.getElementById('live-lens');
    if (!host) return;

    var scene = host.querySelector('[data-live-scene]');
    var glass = host.querySelector('[data-live-glass]');
    var zoomLens = host.querySelector('[data-live-zoom-lens]');
    var zoomDock = host.querySelector('[data-live-zoom-dock]');
    var dock = host.querySelector('[data-live-dock]');
    var cursor = host.querySelector('[data-live-cursor]');
    var modeBtns = document.querySelectorAll('[data-live-mode]');
    if (!scene || !glass || !zoomLens) return;

    var mode = host.getAttribute('data-live-mode') || 'lens';
    var scaleLens = 2.2;
    var scaleDock = 2.4;

    function fillZoom(node) {
      if (!node) return;
      node.innerHTML = '';
      var clone = scene.cloneNode(true);
      clone.removeAttribute('data-live-scene');
      clone.classList.add('live-lens-clone-inner');
      node.appendChild(clone);
    }

    function syncClones() {
      var r = host.getBoundingClientRect();
      if (r.width < 8 || r.height < 8) return;
      fillZoom(zoomLens);
      fillZoom(zoomDock);
      [zoomLens, zoomDock].forEach(function (node) {
        if (!node) return;
        node.style.width = r.width + 'px';
        node.style.height = r.height + 'px';
      });
    }

    function setMode(next) {
      mode = next === 'dock' ? 'dock' : 'lens';
      host.setAttribute('data-live-mode', mode);
      modeBtns.forEach(function (btn) {
        var on = btn.getAttribute('data-live-mode') === mode;
        btn.classList.toggle('is-on', on);
        btn.setAttribute('aria-pressed', on ? 'true' : 'false');
      });
      syncClones();
    }

    modeBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        setMode(btn.getAttribute('data-live-mode'));
      });
    });

    function paint(clientX, clientY) {
      var r = host.getBoundingClientRect();
      if (r.width < 8 || r.height < 8) return;
      var x = clientX - r.left;
      var y = clientY - r.top;
      x = Math.max(0, Math.min(r.width, x));
      y = Math.max(0, Math.min(r.height, y));

      if (mode === 'lens') {
        glass.style.left = x + 'px';
        glass.style.top = y + 'px';
        var gw = glass.offsetWidth || 150;
        var gh = glass.offsetHeight || 150;
        var tx = gw / 2 - x * scaleLens;
        var ty = gh / 2 - y * scaleLens;
        zoomLens.style.transform =
          'translate(' + tx + 'px,' + ty + 'px) scale(' + scaleLens + ')';
      } else {
        if (cursor) {
          cursor.style.left = x + 'px';
          cursor.style.top = y + 'px';
        }
        var view = dock ? dock.querySelector('.live-dock-view') : null;
        if (view && zoomDock) {
          var vr = view.getBoundingClientRect();
          var dtx = vr.width / 2 - x * scaleDock;
          var dty = vr.height / 2 - y * scaleDock;
          zoomDock.style.transform =
            'translate(' + dtx + 'px,' + dty + 'px) scale(' + scaleDock + ')';
        }
      }
    }

    syncClones();

    host.addEventListener('pointermove', function (e) {
      paint(e.clientX, e.clientY);
    });
    host.addEventListener('pointerdown', function (e) {
      try { host.setPointerCapture(e.pointerId); } catch (err) { /* ignore */ }
      paint(e.clientX, e.clientY);
    });

    window.addEventListener('resize', function () {
      syncClones();
      var r = host.getBoundingClientRect();
      paint(r.left + r.width * 0.4, r.top + r.height * 0.45);
    });

    requestAnimationFrame(function () {
      syncClones();
      var r = host.getBoundingClientRect();
      paint(r.left + r.width * 0.4, r.top + r.height * 0.45);
    });
  }

  function initCompare() {
    var root = document.querySelector('[data-compare]');
    if (!root) return;
    var after = root.querySelector('[data-compare-after]');
    var handle = root.querySelector('[data-compare-handle]');
    var range = root.querySelector('[data-compare-range]');
    if (!after || !range) return;

    function setPct(pct) {
      pct = Math.max(0, Math.min(100, pct));
      after.style.clipPath = 'inset(0 0 0 ' + pct + '%)';
      if (handle) handle.style.left = pct + '%';
      range.value = String(Math.round(pct));
      range.setAttribute('aria-valuenow', String(Math.round(pct)));
    }

    range.addEventListener('input', function () {
      setPct(parseFloat(range.value) || 50);
    });

    var dragging = false;
    function fromEvent(e) {
      var r = root.getBoundingClientRect();
      var x = (e.touches ? e.touches[0].clientX : e.clientX) - r.left;
      setPct((x / r.width) * 100);
    }
    root.addEventListener('pointerdown', function (e) {
      dragging = true;
      try { root.setPointerCapture(e.pointerId); } catch (err) { /* ignore */ }
      fromEvent(e);
    });
    root.addEventListener('pointermove', function (e) {
      if (!dragging) return;
      fromEvent(e);
    });
    root.addEventListener('pointerup', function () { dragging = false; });
    root.addEventListener('pointercancel', function () { dragging = false; });

    setPct(52);
  }

  function initHotkeyPlay() {
    var box = document.getElementById('hotkey-play');
    var status = document.getElementById('hotkey-play-status');
    if (!box || !status) return;
    var zoom = 1;
    var MAX = 1.75;
    box.addEventListener('keydown', function (e) {
      if (!(e.ctrlKey && e.altKey)) return;
      if (e.key === 'ArrowUp' || e.key === '+') {
        e.preventDefault();
        zoom = Math.min(MAX, +(zoom + 0.12).toFixed(2));
        box.style.setProperty('--hk-zoom', String(zoom));
        status.textContent = 'Zoom: ' + zoom.toFixed(1) + '×  ·  Ctrl+Alt+↑';
      } else if (e.key === 'ArrowDown' || e.key === '-') {
        e.preventDefault();
        zoom = Math.max(1, +(zoom - 0.12).toFixed(2));
        box.style.setProperty('--hk-zoom', String(zoom));
        status.textContent = 'Zoom: ' + zoom.toFixed(1) + '×  ·  Ctrl+Alt+↓';
      }
    });
  }

  function boot() {
    initLiveStage();
    initCompare();
    initHotkeyPlay();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
