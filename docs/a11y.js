(function () {
  'use strict';

  var KEY = 'forzen-a11y-v1';
  var root = document.documentElement;
  var DEFAULTS = { font: 100, hc: false, legible: false };

  function load() {
    try {
      return JSON.parse(localStorage.getItem(KEY) || '{}') || {};
    } catch (e) {
      return {};
    }
  }

  function save(state) {
    try {
      localStorage.setItem(KEY, JSON.stringify(state));
    } catch (e) { /* ignore */ }
  }

  function clearStorage() {
    try {
      localStorage.removeItem(KEY);
    } catch (e) { /* ignore */ }
  }

  function apply(state) {
    root.setAttribute('data-font-scale', String(state.font || 100));
    if (state.hc) root.setAttribute('data-hc', 'true');
    else root.removeAttribute('data-hc');
    if (state.legible) root.setAttribute('data-legible', 'true');
    else root.removeAttribute('data-legible');

    document.querySelectorAll('[data-a11y-font]').forEach(function (btn) {
      var on = String(btn.getAttribute('data-a11y-font')) === String(state.font || 100);
      btn.setAttribute('aria-pressed', on ? 'true' : 'false');
      btn.classList.toggle('is-on', on);
    });
    document.querySelectorAll('[data-a11y-hc]').forEach(function (btn) {
      btn.setAttribute('aria-pressed', state.hc ? 'true' : 'false');
      btn.classList.toggle('is-on', !!state.hc);
    });
    document.querySelectorAll('[data-a11y-legible]').forEach(function (btn) {
      btn.setAttribute('aria-pressed', state.legible ? 'true' : 'false');
      btn.classList.toggle('is-on', !!state.legible);
    });
  }

  var state = load();
  if (!state.font) state.font = 100;
  apply(state);

  function bind() {
    var toggle = document.querySelector('.a11y-toggle');
    var panel = document.getElementById('a11y-panel');
    if (toggle && panel) {
      toggle.addEventListener('click', function () {
        if (panel.hasAttribute('hidden')) {
          panel.removeAttribute('hidden');
          toggle.setAttribute('aria-expanded', 'true');
        } else {
          panel.setAttribute('hidden', '');
          toggle.setAttribute('aria-expanded', 'false');
        }
      });
      document.addEventListener('click', function (e) {
        if (!panel || panel.hasAttribute('hidden')) return;
        var bar = document.querySelector('.a11y-bar');
        if (bar && !bar.contains(e.target)) {
          panel.setAttribute('hidden', '');
          toggle.setAttribute('aria-expanded', 'false');
        }
      });
    }

    document.querySelectorAll('[data-a11y-font]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state.font = parseInt(btn.getAttribute('data-a11y-font'), 10) || 100;
        save(state);
        apply(state);
      });
    });
    document.querySelectorAll('[data-a11y-hc]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state.hc = !state.hc;
        save(state);
        apply(state);
      });
    });
    document.querySelectorAll('[data-a11y-legible]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state.legible = !state.legible;
        save(state);
        apply(state);
      });
    });
    document.querySelectorAll('[data-a11y-reset]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state = { font: DEFAULTS.font, hc: DEFAULTS.hc, legible: DEFAULTS.legible };
        clearStorage();
        apply(state);
        var prev = btn.textContent;
        btn.textContent = 'Restaurado';
        setTimeout(function () { btn.textContent = prev; }, 1400);
      });
    });

    document.querySelectorAll('[data-copy-sha]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var sel = btn.getAttribute('data-copy-sha');
        var el = sel ? document.querySelector(sel) : null;
        var text = el ? (el.textContent || '').trim() : '';
        if (!text) return;
        function ok() {
          var prev = btn.textContent;
          btn.textContent = 'Copiado';
          setTimeout(function () { btn.textContent = prev; }, 1600);
        }
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text).then(ok).catch(function () {
            fallbackCopy(text); ok();
          });
        } else {
          fallbackCopy(text); ok();
        }
      });
    });

    // Audio summary (Web Speech API)
    var speakBtn = document.getElementById('audio-summary-btn');
    if (speakBtn && 'speechSynthesis' in window) {
      var speaking = false;
      speakBtn.addEventListener('click', function () {
        if (speaking) {
          window.speechSynthesis.cancel();
          speaking = false;
          speakBtn.textContent = 'Escuchar resumen';
          speakBtn.setAttribute('aria-pressed', 'false');
          return;
        }
        var text = speakBtn.getAttribute('data-speech') ||
          'Forzen es una lupa de pantalla gratuita para Windows. Zoom de 1 a 8 veces, modos lupa y acoplado, filtros de color y atajos personalizables. Descarga el instalador sin necesidad de instalar Java.';
        var u = new SpeechSynthesisUtterance(text);
        u.lang = 'es-ES';
        u.rate = 1;
        u.onend = function () {
          speaking = false;
          speakBtn.textContent = 'Escuchar resumen';
          speakBtn.setAttribute('aria-pressed', 'false');
        };
        speaking = true;
        speakBtn.textContent = 'Detener audio';
        speakBtn.setAttribute('aria-pressed', 'true');
        window.speechSynthesis.cancel();
        window.speechSynthesis.speak(u);
      });
    } else if (speakBtn) {
      speakBtn.hidden = true;
    }
  }

  function fallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand('copy'); } catch (e) { /* ignore */ }
    document.body.removeChild(ta);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
