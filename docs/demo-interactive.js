(function () {
  'use strict';

  function initLiveLens() {
    var host = document.getElementById('live-lens');
    if (!host) return;
    var scene = host.querySelector('[data-live-scene]');
    var glass = host.querySelector('[data-live-glass]');
    var zoom = host.querySelector('[data-live-zoom]');
    if (!scene || !glass || !zoom) return;

    var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var scale = 2.2;

    function syncClone() {
      zoom.innerHTML = '';
      var clone = scene.cloneNode(true);
      clone.removeAttribute('data-live-scene');
      clone.classList.add('live-lens-clone-inner');
      zoom.appendChild(clone);
      var r = host.getBoundingClientRect();
      zoom.style.width = r.width + 'px';
      zoom.style.height = r.height + 'px';
    }

    function paint(clientX, clientY) {
      var r = host.getBoundingClientRect();
      var x = clientX - r.left;
      var y = clientY - r.top;
      x = Math.max(0, Math.min(r.width, x));
      y = Math.max(0, Math.min(r.height, y));
      glass.style.left = x + 'px';
      glass.style.top = y + 'px';
      var gr = glass.getBoundingClientRect();
      var tx = gr.width / 2 - x * scale;
      var ty = gr.height / 2 - y * scale;
      zoom.style.transform = 'translate(' + tx + 'px,' + ty + 'px) scale(' + scale + ')';
    }

    syncClone();
    if (reduce) {
      paint(host.clientWidth * 0.45, host.clientHeight * 0.45);
      return;
    }

    host.addEventListener('mousemove', function (e) {
      paint(e.clientX, e.clientY);
    });
    host.addEventListener('mouseenter', function () {
      glass.classList.add('is-on');
    });
    host.addEventListener('mouseleave', function () {
      glass.classList.remove('is-on');
    });
    window.addEventListener('resize', function () {
      syncClone();
    });
    // Initial center
    requestAnimationFrame(function () {
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
    box.addEventListener('keydown', function (e) {
      // Only when focused inside the demo box
      if (!(e.ctrlKey && e.altKey)) return;
      if (e.key === 'ArrowUp' || e.key === '+') {
        e.preventDefault();
        zoom = Math.min(2.5, zoom + 0.15);
        box.style.setProperty('--hk-zoom', String(zoom));
        status.textContent = 'Zoom simulado: ' + zoom.toFixed(1) + '× (Ctrl+Alt+↑)';
      } else if (e.key === 'ArrowDown' || e.key === '-') {
        e.preventDefault();
        zoom = Math.max(1, zoom - 0.15);
        box.style.setProperty('--hk-zoom', String(zoom));
        status.textContent = 'Zoom simulado: ' + zoom.toFixed(1) + '× (Ctrl+Alt+↓)';
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
