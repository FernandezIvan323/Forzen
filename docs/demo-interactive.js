(function () {
  'use strict';

  function initLiveLens() {
    var host = document.getElementById('live-lens');
    if (!host) return;
    var scene = host.querySelector('[data-live-scene]');
    var glass = host.querySelector('[data-live-glass]');
    var zoom = host.querySelector('[data-live-zoom]');
    if (!scene || !glass || !zoom) return;

    var scale = 2.15;

    function syncClone() {
      var r = host.getBoundingClientRect();
      if (r.width < 8 || r.height < 8) return;
      zoom.innerHTML = '';
      var clone = scene.cloneNode(true);
      clone.removeAttribute('data-live-scene');
      clone.classList.add('live-lens-clone-inner');
      // Match scene size exactly for correct hot-point math
      zoom.style.width = r.width + 'px';
      zoom.style.height = r.height + 'px';
      zoom.appendChild(clone);
    }

    function paint(clientX, clientY) {
      var r = host.getBoundingClientRect();
      if (r.width < 8 || r.height < 8) return;
      var x = clientX - r.left;
      var y = clientY - r.top;
      x = Math.max(0, Math.min(r.width, x));
      y = Math.max(0, Math.min(r.height, y));

      glass.style.left = x + 'px';
      glass.style.top = y + 'px';

      // Glass is centered via negative margin; size is fixed in CSS
      var gw = glass.offsetWidth || 140;
      var gh = glass.offsetHeight || 140;
      var tx = gw / 2 - x * scale;
      var ty = gh / 2 - y * scale;
      zoom.style.transform = 'translate(' + tx + 'px,' + ty + 'px) scale(' + scale + ')';
    }

    syncClone();

    // Always follow pointer (mouse + touch)
    host.addEventListener('pointermove', function (e) {
      paint(e.clientX, e.clientY);
    });
    host.addEventListener('pointerdown', function (e) {
      host.setPointerCapture(e.pointerId);
      paint(e.clientX, e.clientY);
    });

    window.addEventListener('resize', function () {
      syncClone();
      var r = host.getBoundingClientRect();
      paint(r.left + r.width * 0.42, r.top + r.height * 0.48);
    });

    // Center start
    requestAnimationFrame(function () {
      syncClone();
      var r = host.getBoundingClientRect();
      paint(r.left + r.width * 0.42, r.top + r.height * 0.48);
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
      root.setPointerCapture(e.pointerId);
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
    initLiveLens();
    initCompare();
    initHotkeyPlay();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
