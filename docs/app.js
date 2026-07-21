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
   * Product demos — JS-driven motion (always runs).
   * CSS animations were blocked by prefers-reduced-motion / OS settings
   * and looked frozen. Inline left/top updates always paint.
   */
  function initFxDemos() {
    var demos = document.querySelectorAll('.fx-demo[data-fx]');
    if (!demos.length) return;

    var pathLens = [
      [0.30, 0.40],
      [0.50, 0.28],
      [0.70, 0.46],
      [0.55, 0.66],
      [0.28, 0.58],
      [0.30, 0.40]
    ];
    var pathDock = [
      [0.28, 0.36],
      [0.50, 0.32],
      [0.66, 0.50],
      [0.42, 0.64],
      [0.26, 0.48],
      [0.28, 0.36]
    ];

    function lerp(a, b, t) { return a + (b - a) * t; }
    function ease(t) {
      return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }
    function sample(path, t) {
      var n = path.length - 1;
      var x = ((t % 1) + 1) % 1 * n;
      var i = Math.min(Math.floor(x), n - 1);
      var local = ease(x - i);
      return {
        x: lerp(path[i][0], path[i + 1][0], local),
        y: lerp(path[i][1], path[i + 1][1], local)
      };
    }

    function setup(demo) {
      var mode = demo.getAttribute('data-fx');
      var path = mode === 'dock' ? pathDock : pathLens;
      var duration = mode === 'dock' ? 9000 : 7500;
      var hot = demo.querySelector('[data-fx-hot]');
      var zoom = demo.querySelector('[data-fx-zoom]');
      var dockCard = demo.querySelector('[data-fx-dock-card]');
      if (!hot) return;

      var t0 = 0;

      function paint(pt) {
        hot.style.left = (pt.x * 100).toFixed(2) + '%';
        hot.style.top = (pt.y * 100).toFixed(2) + '%';
        // Pan magnified content opposite to motion (feels linked)
        if (zoom) {
          zoom.style.transform =
            'translate(' + (-pt.x * 55).toFixed(1) + '%, ' + (-pt.y * 45).toFixed(1) + '%)';
        }
        if (dockCard) {
          dockCard.style.transform =
            'translate(' + (-pt.x * 40 + 10).toFixed(1) + '%, ' + (-pt.y * 30 + 5).toFixed(1) + '%)';
        }
      }

      // Immediate first frame
      paint({ x: path[0][0], y: path[0][1] });

      // Always-on loop (no pause) — OS "reduce motion" blocked pure CSS before
      function frame(now) {
        if (!t0) t0 = now;
        paint(sample(path, (now - t0) / duration));
        requestAnimationFrame(frame);
      }
      requestAnimationFrame(frame);
    }

    demos.forEach(setup);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initFxDemos);
  } else {
    initFxDemos();
  }
})();
