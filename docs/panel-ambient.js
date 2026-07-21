/**
 * Panel ambient: full-page height + right-rail placement + decorative motion.
 * Windows "disable animations" sets prefers-reduced-motion; we still animate
 * this decorative layer unless the user turns it off on the page (A·a).
 */
(function () {
  'use strict';

  var KEY = 'forzen-panel-ambient-motion';
  var ambient = document.querySelector('.pc-ambient');
  var shell = document.querySelector('.pc-page-shell');
  if (!ambient || !shell) return;

  var lenses = Array.prototype.slice.call(ambient.querySelectorAll('.pc-float-lens'));
  var hero = ambient.querySelector('.pc-hero-lens');
  var glows = Array.prototype.slice.call(ambient.querySelectorAll('.pc-ambient-glow'));

  function motionEnabled() {
    try {
      var v = localStorage.getItem(KEY);
      if (v === '0') return false;
      if (v === '1') return true;
    } catch (e) { /* ignore */ }
    return true; // default ON even if OS reduce-motion is on
  }

  function setMotion(on) {
    ambient.classList.toggle('pc-ambient--animate', on);
    try {
      localStorage.setItem(KEY, on ? '1' : '0');
    } catch (e) { /* ignore */ }
    document.querySelectorAll('[data-a11y-ambient]').forEach(function (btn) {
      btn.setAttribute('aria-pressed', on ? 'true' : 'false');
      btn.classList.toggle('is-on', on);
    });
  }

  function contentRight() {
    var mainCol = document.querySelector('.pc-main') || document.querySelector('.pc-layout');
    var toc = document.querySelector('.pc-toc');
    var right = 0;
    if (mainCol) {
      var r = mainCol.getBoundingClientRect();
      right = Math.max(right, r.right);
    }
    if (toc) {
      var t = toc.getBoundingClientRect();
      right = Math.max(right, t.right);
    }
    // document X (not viewport)
    return right + window.scrollX;
  }

  function layout() {
    var H = Math.max(shell.scrollHeight, document.documentElement.scrollHeight, window.innerHeight);
    ambient.style.top = '0';
    ambient.style.left = '0';
    ambient.style.right = '0';
    ambient.style.bottom = 'auto';
    ambient.style.height = H + 'px';
    ambient.style.width = '100%';

    var docW = Math.max(shell.clientWidth, document.documentElement.clientWidth);
    var colRight = contentRight();
    var gutterLeft = colRight + 28;
    var gutterWidth = docW - gutterLeft - 16;

    // If no real gutter (narrow), park on far right edge
    var useEdge = gutterWidth < 72;
    var parkLeft = useEdge ? Math.max(16, docW - 88) : gutterLeft;
    var parkSpan = useEdge ? 40 : Math.max(40, gutterWidth - 40);

    var fracs = [0.06, 0.16, 0.26, 0.36, 0.46, 0.56, 0.66, 0.76, 0.88];
    lenses.forEach(function (el, i) {
      var f = fracs[i % fracs.length];
      var top = Math.round(H * f);
      var size = 40 + (i % 4) * 10;
      if (useEdge) size = Math.min(size, 48);
      var xOff = (i * 37) % Math.max(1, Math.floor(parkSpan));
      el.style.position = 'absolute';
      el.style.top = top + 'px';
      el.style.left = Math.round(parkLeft + xOff) + 'px';
      el.style.right = 'auto';
      el.style.width = size + 'px';
      el.style.height = size + 'px';
      el.style.opacity = useEdge ? '0.32' : String(0.36 + (i % 3) * 0.04);
      el.style.display = 'block';
    });

    if (hero) {
      var heroSize = useEdge ? 72 : 110;
      hero.style.position = 'absolute';
      hero.style.width = heroSize + 'px';
      hero.style.height = heroSize + 'px';
      hero.style.left = Math.round(parkLeft + Math.min(24, parkSpan * 0.2)) + 'px';
      hero.style.right = 'auto';
      hero.style.top = Math.round(H * 0.08) + 'px';
      hero.style.opacity = useEdge ? '0.34' : '0.46';
      // Custom property for vertical travel in CSS (px of page)
      hero.style.setProperty('--pc-hero-travel', Math.round(H * 0.72) + 'px');
    }

    glows.forEach(function (g, i) {
      g.style.position = 'absolute';
      g.style.left = Math.round(parkLeft - 40) + 'px';
      g.style.right = 'auto';
      g.style.top = Math.round(H * (i === 0 ? 0.1 : 0.55)) + 'px';
    });
  }

  function bindToggle() {
    document.querySelectorAll('[data-a11y-ambient]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        setMotion(!ambient.classList.contains('pc-ambient--animate'));
      });
    });
  }

  var ro;
  function boot() {
    setMotion(motionEnabled());
    layout();
    bindToggle();
    window.addEventListener('resize', layout);
    window.addEventListener('load', layout);
    // fonts / late layout
    setTimeout(layout, 100);
    setTimeout(layout, 500);
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(function () { layout(); });
      ro.observe(shell);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
