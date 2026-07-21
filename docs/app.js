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

  // Pause product demos when off-screen (saves battery; CSS animations)
  if ('IntersectionObserver' in window && !reduceMotion) {
    var demos = document.querySelectorAll('.fx-demo');
    var dio = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        entry.target.style.animationPlayState = entry.isIntersecting ? 'running' : 'paused';
        entry.target.querySelectorAll(
          '.fx-cursor, .fx-lens, .fx-lens-zoom, .fx-dock-zoom .fx-zoom-card'
        ).forEach(function (el) {
          el.style.animationPlayState = entry.isIntersecting ? 'running' : 'paused';
        });
      });
    }, { threshold: 0.15 });
    demos.forEach(function (d) { dio.observe(d); });
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
})();
