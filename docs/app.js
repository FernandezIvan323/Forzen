(function () {
  'use strict';

  // Mobile nav
  var toggle = document.querySelector('.nav-toggle');
  var nav = document.getElementById('primary-nav');
  if (toggle && nav) {
    toggle.addEventListener('click', function () {
      var open = nav.classList.toggle('open');
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
      toggle.setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú');
    });
    nav.querySelectorAll('a').forEach(function (link) {
      link.addEventListener('click', function () {
        nav.classList.remove('open');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Abrir menú');
      });
    });
  }

  // Reveal on scroll
  var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var reveals = document.querySelectorAll('.reveal');
  if (reduceMotion) {
    reveals.forEach(function (el) { el.classList.add('is-visible'); });
  } else if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
    reveals.forEach(function (el) { io.observe(el); });
  } else {
    reveals.forEach(function (el) { el.classList.add('is-visible'); });
  }

  // Stat counter
  var statNum = document.getElementById('stat-counter');
  if (statNum && 'IntersectionObserver' in window && !reduceMotion) {
    var done = false;
    var so = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting || done) return;
        done = true;
        var current = 0;
        var target = 285;
        var step = Math.ceil(target / 50);
        var timer = setInterval(function () {
          current += step;
          if (current >= target) {
            current = target;
            clearInterval(timer);
          }
          statNum.textContent = current + 'M';
        }, 24);
        so.unobserve(entry.target);
      });
    }, { threshold: 0.5 });
    so.observe(statNum);
  }

  /**
   * Animated product demos: lens (circular magnifier) and docked panel.
   * Clones the scene into the zoom viewport and follows a smooth path.
   */
  function initProductDemos() {
    var scenes = document.querySelectorAll('.demo-scene[data-demo]');
    if (!scenes.length) return;

    // Path points as fractions of scene size (cursor hot-point)
    var pathLens = [
      { x: 0.28, y: 0.38 },
      { x: 0.48, y: 0.30 },
      { x: 0.68, y: 0.42 },
      { x: 0.58, y: 0.62 },
      { x: 0.34, y: 0.58 },
      { x: 0.28, y: 0.38 }
    ];
    var pathDock = [
      { x: 0.30, y: 0.36 },
      { x: 0.52, y: 0.32 },
      { x: 0.70, y: 0.48 },
      { x: 0.48, y: 0.66 },
      { x: 0.26, y: 0.52 },
      { x: 0.30, y: 0.36 }
    ];

    function cloneScene(scene) {
      var source = scene.querySelector('[data-scene]');
      var targets = scene.querySelectorAll('[data-zoom-scene]');
      if (!source || !targets.length) return;
      targets.forEach(function (t) {
        t.innerHTML = '';
        // Clone only inner apps so the zoom layer is a same-size world
        var clone = source.cloneNode(true);
        clone.removeAttribute('data-scene');
        clone.classList.add('scene-world--zoom-inner');
        t.appendChild(clone);
      });
    }

    function lerp(a, b, t) {
      return a + (b - a) * t;
    }

    function easeInOut(t) {
      return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    function samplePath(path, t) {
      // t in [0,1) loops along segments of equal duration
      var n = path.length - 1;
      var x = t * n;
      var i = Math.min(Math.floor(x), n - 1);
      var local = easeInOut(x - i);
      return {
        x: lerp(path[i].x, path[i + 1].x, local),
        y: lerp(path[i].y, path[i + 1].y, local)
      };
    }

    function setupScene(scene) {
      cloneScene(scene);

      var mode = scene.getAttribute('data-demo');
      var path = mode === 'dock' ? pathDock : pathLens;
      var zoom = mode === 'dock' ? 2.35 : 2.2;
      scene.style.setProperty('--zoom', String(zoom));

      var cursor = scene.querySelector('[data-cursor]');
      var lens = scene.querySelector('[data-lens]');
      var dock = scene.querySelector('[data-dock]');
      var zoomNodes = scene.querySelectorAll('[data-zoom-scene]');
      var started = false;
      var raf = 0;
      var t0 = 0;
      var duration = mode === 'dock' ? 9000 : 8000;

      function sizeZoomLayers() {
        var rect = scene.getBoundingClientRect();
        var w = rect.width;
        var h = rect.height;
        if (w < 2 || h < 2) return { w: w, h: h };
        zoomNodes.forEach(function (node) {
          node.style.width = w + 'px';
          node.style.height = h + 'px';
        });
        return { w: w, h: h };
      }

      function paint(fracX, fracY) {
        var dims = sizeZoomLayers();
        var w = dims.w;
        var h = dims.h;
        if (w < 2 || h < 2) return;

        var pctX = (fracX * 100).toFixed(2) + '%';
        var pctY = (fracY * 100).toFixed(2) + '%';
        scene.style.setProperty('--hot-x', pctX);
        scene.style.setProperty('--hot-y', pctY);

        if (cursor) {
          cursor.style.left = pctX;
          cursor.style.top = pctY;
        }
        if (lens) {
          lens.style.left = pctX;
          lens.style.top = pctY;
          var lr = lens.getBoundingClientRect();
          var lx = fracX * w;
          var ly = fracY * h;
          // Center magnified scene so hot-point sits at lens center
          var tx = lr.width / 2 - lx * zoom;
          var ty = lr.height / 2 - ly * zoom;
          zoomNodes.forEach(function (node) {
            node.style.transform = 'translate(' + tx + 'px,' + ty + 'px) scale(' + zoom + ')';
          });
        }
        if (dock) {
          var dr = dock.querySelector('.demo-dock-view');
          if (!dr) return;
          var drect = dr.getBoundingClientRect();
          var cx = fracX * w;
          var cy = fracY * h;
          var dtx = drect.width / 2 - cx * zoom;
          var dty = drect.height / 2 - cy * zoom;
          zoomNodes.forEach(function (node) {
            node.style.transform = 'translate(' + dtx + 'px,' + dty + 'px) scale(' + zoom + ')';
          });
        }
      }

      function frame(now) {
        if (!t0) t0 = now;
        var t = ((now - t0) % duration) / duration;
        var pt = samplePath(path, t);
        paint(pt.x, pt.y);
        raf = requestAnimationFrame(frame);
      }

      function start() {
        if (started) return;
        started = true;
        sizeZoomLayers();
        if (reduceMotion) {
          paint(0.45, 0.48);
          return;
        }
        raf = requestAnimationFrame(frame);
      }

      function stop() {
        if (raf) cancelAnimationFrame(raf);
        raf = 0;
        started = false;
        t0 = 0;
      }

      // Static first paint so layout isn't empty before IO
      paint(path[0].x, path[0].y);

      if ('IntersectionObserver' in window) {
        var dio = new IntersectionObserver(function (entries) {
          entries.forEach(function (entry) {
            if (entry.isIntersecting) start();
            else stop();
          });
        }, { threshold: 0.2 });
        dio.observe(scene);
      } else {
        start();
      }

      window.addEventListener('resize', function () {
        sizeZoomLayers();
      });
    }

    scenes.forEach(setupScene);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initProductDemos);
  } else {
    initProductDemos();
  }
})();
