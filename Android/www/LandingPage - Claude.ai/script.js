/* ============================================================
   VitalSense — script.js
   Libraries: GSAP + ScrollTrigger, Lenis, SplitType
   ============================================================ */

/* ---------- 1. LENIS SMOOTH SCROLL ---------- */
const lenis = new Lenis({
  duration: 1.2,
  easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
  direction: 'vertical',
  smooth: true,
});

function raf(time) {
  lenis.raf(time);
  requestAnimationFrame(raf);
}
requestAnimationFrame(raf);

// Anchor link click — let Lenis handle smooth scroll
document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
  anchor.addEventListener('click', (e) => {
    const target = document.querySelector(anchor.getAttribute('href'));
    if (target) {
      e.preventDefault();
      lenis.scrollTo(target, { offset: -80 });
    }
  });
});

/* ---------- 2. GSAP + ScrollTrigger SETUP ---------- */
gsap.registerPlugin(ScrollTrigger);

// Sync Lenis with ScrollTrigger
lenis.on('scroll', ScrollTrigger.update);
gsap.ticker.add((time) => { lenis.raf(time * 1000); });
gsap.ticker.lagSmoothing(0);

/* ---------- 3. NAV SCROLL EFFECT ---------- */
const nav = document.getElementById('nav');
ScrollTrigger.create({
  start: 80,
  onEnter: () => nav.classList.add('scrolled'),
  onLeaveBack: () => nav.classList.remove('scrolled'),
});

/* ---------- 4. MOBILE NAV TOGGLE ---------- */
const navToggle = document.getElementById('navToggle');
const mobileNav = document.getElementById('mobileNav');
let mobileOpen = false;

navToggle.addEventListener('click', () => {
  mobileOpen = !mobileOpen;
  mobileNav.classList.toggle('open', mobileOpen);
  // Animate hamburger spans
  const spans = navToggle.querySelectorAll('span');
  if (mobileOpen) {
    gsap.to(spans[0], { y: 7, rotate: 45, duration: .3 });
    gsap.to(spans[1], { opacity: 0, duration: .2 });
    gsap.to(spans[2], { y: -7, rotate: -45, duration: .3 });
    lenis.stop();
  } else {
    gsap.to(spans, { y: 0, rotate: 0, opacity: 1, duration: .3 });
    lenis.start();
  }
});

document.querySelectorAll('.mobile-link').forEach((link) => {
  link.addEventListener('click', () => {
    mobileOpen = false;
    mobileNav.classList.remove('open');
    const spans = navToggle.querySelectorAll('span');
    gsap.to(spans, { y: 0, rotate: 0, opacity: 1, duration: .3 });
    lenis.start();
  });
});

/* ---------- 5. HERO ENTRANCE ANIMATIONS ---------- */
function initHero() {
  // SplitType on the headline
  const headline = document.getElementById('heroHeadline');
  if (!headline) return;

  const splitHero = new SplitType(headline, { types: 'lines,words,chars' });

  const tl = gsap.timeline({ delay: .3 });

  // Badge
  tl.from('.hero-badge', { opacity: 0, y: 20, duration: .6, ease: 'power3.out' });

  // Headline chars come in staggered
  tl.from(splitHero.chars, {
    opacity: 0,
    y: 40,
    rotateX: -30,
    stagger: 0.02,
    duration: .7,
    ease: 'power3.out',
  }, '-=.3');

  // Sub text, CTAs, stats
  tl.from('.hero-sub', { opacity: 0, y: 20, duration: .6, ease: 'power3.out' }, '-=.3');
  tl.from('.hero-actions', { opacity: 0, y: 20, duration: .5, ease: 'power3.out' }, '-=.3');
  tl.from('.hero-stats', { opacity: 0, y: 20, duration: .5, ease: 'power3.out' }, '-=.4');

  // Product image
  tl.from('#heroProduct', {
    opacity: 0,
    scale: .8,
    y: 60,
    duration: 1,
    ease: 'power3.out',
  }, '-=.8');

  // Rings & halo
  tl.from('.ring', {
    opacity: 0,
    scale: .5,
    stagger: .1,
    duration: .8,
    ease: 'power3.out',
  }, '-=.6');

  // Reading cards
  tl.from('#bpmCard', { opacity: 0, x: -30, duration: .6, ease: 'back.out(2)' }, '-=.4');
  tl.from('#tempCard', { opacity: 0, x: 30, duration: .6, ease: 'back.out(2)' }, '-=.5');

  // Scroll hint
  tl.from('.scroll-hint', { opacity: 0, y: -10, duration: .5 }, '-=.2');
}

initHero();

/* ---------- 6. LIVE BPM & TEMP COUNTER ANIMATION ---------- */
function animateLiveReadings() {
  const bpmEl = document.getElementById('bpmVal');
  const tempEl = document.getElementById('tempVal');
  let bpmObj = { val: 72 };
  let tempObj = { val: 36.5 };

  // Animate BPM value up and back — simulating a heartbeat cycle
  function pulseBPM() {
    gsap.to(bpmObj, {
      val: 68 + Math.floor(Math.random() * 15),
      duration: 1.5,
      ease: 'power2.inOut',
      onUpdate: () => { bpmEl.textContent = Math.round(bpmObj.val); },
      onComplete: () => { setTimeout(pulseBPM, 2000 + Math.random() * 2000); },
    });
  }

  function pulseTemp() {
    const newTemp = (36.3 + Math.random() * .4).toFixed(1);
    gsap.to(tempObj, {
      val: parseFloat(newTemp),
      duration: 2,
      ease: 'power2.inOut',
      onUpdate: () => { tempEl.textContent = tempObj.val.toFixed(1) + '°'; },
      onComplete: () => { setTimeout(pulseTemp, 3000 + Math.random() * 2000); },
    });
  }

  setTimeout(pulseBPM, 2000);
  setTimeout(pulseTemp, 2500);
}

animateLiveReadings();

/* ---------- 7. SECTION TITLE SPLIT ANIMATION ---------- */
document.querySelectorAll('[data-split]').forEach((el) => {
  const split = new SplitType(el, { types: 'lines,words' });

  gsap.from(split.words, {
    scrollTrigger: {
      trigger: el,
      start: 'top 85%',
    },
    opacity: 0,
    y: 40,
    rotateX: -20,
    stagger: .05,
    duration: .7,
    ease: 'power3.out',
  });
});

/* ---------- 8. DATA-REVEAL ELEMENTS ---------- */
document.querySelectorAll('[data-reveal]').forEach((el) => {
  gsap.to(el, {
    scrollTrigger: {
      trigger: el,
      start: 'top 88%',
    },
    opacity: 1,
    y: 0,
    duration: .7,
    ease: 'power3.out',
  });
});

/* ---------- 9. BENEFIT CARDS ---------- */
gsap.utils.toArray('[data-benefit]').forEach((card, i) => {
  gsap.to(card, {
    scrollTrigger: {
      trigger: card,
      start: 'top 90%',
    },
    opacity: 1,
    y: 0,
    duration: .6,
    delay: (i % 3) * .1,
    ease: 'power3.out',
  });
});

/* ---------- 10. HOW IT WORKS STEPS ---------- */
gsap.utils.toArray('[data-step]').forEach((step, i) => {
  gsap.to(step, {
    scrollTrigger: {
      trigger: step,
      start: 'top 88%',
    },
    opacity: 1,
    x: 0,
    duration: .7,
    delay: i * .1,
    ease: 'power3.out',
  });
});

/* ---------- 11. FEATURE CARDS ---------- */
gsap.utils.toArray('[data-feature]').forEach((card, i) => {
  gsap.to(card, {
    scrollTrigger: {
      trigger: card,
      start: 'top 90%',
    },
    opacity: 1,
    y: 0,
    scale: 1,
    duration: .6,
    delay: (i % 3) * .1,
    ease: 'back.out(1.5)',
  });
});

/* ---------- 12. SHOWCASE PARALLAX ---------- */
const showcaseImg = document.getElementById('showcaseProduct');
if (showcaseImg) {
  gsap.to(showcaseImg, {
    scrollTrigger: {
      trigger: '.showcase',
      start: 'top bottom',
      end: 'bottom top',
      scrub: 1.5,
    },
    y: -60,
    rotate: 5,
    ease: 'none',
  });
}

/* Showcase section reveal */
ScrollTrigger.create({
  trigger: '.showcase',
  start: 'top 75%',
  onEnter: () => {
    gsap.to('.showcase-text [data-reveal]', {
      opacity: 1, y: 0, stagger: .12, duration: .7, ease: 'power3.out'
    });
    gsap.from('.showcase-img', {
      opacity: 0, scale: .85, x: 40, duration: 1, ease: 'power3.out'
    });
  }
});

/* ---------- 13. PRICING CARDS ---------- */
gsap.utils.toArray('[data-price]').forEach((card, i) => {
  gsap.to(card, {
    scrollTrigger: {
      trigger: card,
      start: 'top 90%',
    },
    opacity: 1,
    y: 0,
    duration: .65,
    delay: i * .15,
    ease: 'power3.out',
  });
});

/* ---------- 14. CTA PRODUCT FLOAT ---------- */
const ctaProduct = document.getElementById('ctaProduct');
if (ctaProduct) {
  gsap.from(ctaProduct, {
    scrollTrigger: {
      trigger: '.cta-final',
      start: 'top 80%',
    },
    opacity: 0,
    x: -60,
    rotate: -10,
    duration: 1,
    ease: 'power3.out',
  });

  // Parallax while scrolling
  gsap.to(ctaProduct, {
    scrollTrigger: {
      trigger: '.cta-final',
      start: 'top bottom',
      end: 'bottom top',
      scrub: 2,
    },
    y: -40,
    rotate: 6,
    ease: 'none',
  });
}

/* ---------- 15. FAQ ACCORDION ---------- */
document.querySelectorAll('[data-faq]').forEach((item, i) => {
  const btn = item.querySelector('.faq-q');
  const answer = item.querySelector('.faq-a');

  // Scroll reveal
  gsap.to(item, {
    scrollTrigger: { trigger: item, start: 'top 90%' },
    opacity: 1, y: 0, duration: .5, delay: i * .06, ease: 'power3.out',
  });

  btn.addEventListener('click', () => {
    const isOpen = btn.getAttribute('aria-expanded') === 'true';

    // Close all
    document.querySelectorAll('.faq-q').forEach((b) => b.setAttribute('aria-expanded', 'false'));
    document.querySelectorAll('.faq-a').forEach((a) => a.classList.remove('open'));

    // Open clicked if it was closed
    if (!isOpen) {
      btn.setAttribute('aria-expanded', 'true');
      answer.classList.add('open');
    }
  });
});

/* ---------- 16. BENEFIT CARD HOVER PARTICLE ---------- */
document.querySelectorAll('.benefit-card').forEach((card) => {
  card.addEventListener('mousemove', (e) => {
    const rect = card.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    card.style.setProperty('--mx', `${x}px`);
    card.style.setProperty('--my', `${y}px`);
  });
});

/* ---------- 17. HERO SECTION PARALLAX ON SCROLL ---------- */
gsap.to('.hero-visual', {
  scrollTrigger: {
    trigger: '.hero',
    start: 'top top',
    end: 'bottom top',
    scrub: 1,
  },
  y: 80,
  ease: 'none',
});

gsap.to('.hero-text', {
  scrollTrigger: {
    trigger: '.hero',
    start: 'top top',
    end: 'bottom top',
    scrub: 1.5,
  },
  y: 50,
  opacity: 0,
  ease: 'none',
});

/* ---------- 18. TRUST BADGES STAGGER ---------- */
gsap.utils.toArray('.trust-badge').forEach((badge, i) => {
  gsap.to(badge, {
    scrollTrigger: { trigger: badge, start: 'top 90%' },
    opacity: 1, y: 0, duration: .6, delay: i * .1, ease: 'power3.out',
  });
});

/* ---------- 19. SECTION BACKGROUND LINE ANIMATION ---------- */
// Animate the step line
ScrollTrigger.create({
  trigger: '.steps-wrap',
  start: 'top 70%',
  onEnter: () => {
    gsap.from('.steps-line', { scaleY: 0, transformOrigin: 'top', duration: 1.5, ease: 'power3.inOut' });
  }
});

/* ---------- 20. FEATURE CARD 3D TILT ---------- */
document.querySelectorAll('.feature-card').forEach((card) => {
  card.addEventListener('mousemove', (e) => {
    const rect = card.getBoundingClientRect();
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;
    const dx = (e.clientX - cx) / (rect.width / 2);
    const dy = (e.clientY - cy) / (rect.height / 2);

    gsap.to(card, {
      rotateY: dx * 8,
      rotateX: -dy * 8,
      duration: .4,
      ease: 'power2.out',
      transformPerspective: 800,
    });
  });

  card.addEventListener('mouseleave', () => {
    gsap.to(card, {
      rotateY: 0,
      rotateX: 0,
      duration: .5,
      ease: 'power3.out',
    });
  });
});

/* ---------- 21. PRICE CARD HOVER GLOW ---------- */
document.querySelectorAll('.price-card').forEach((card) => {
  card.addEventListener('mouseenter', () => {
    gsap.to(card, { borderColor: 'rgba(0,201,200,.4)', duration: .3 });
  });
  card.addEventListener('mouseleave', () => {
    if (!card.classList.contains('featured')) {
      gsap.to(card, { borderColor: 'rgba(255,255,255,.08)', duration: .3 });
    }
  });
});

/* ---------- 22. SCROLL PROGRESS BAR ---------- */
const progressBar = document.createElement('div');
progressBar.style.cssText = `
  position: fixed;
  top: 0; left: 0;
  height: 2px;
  background: linear-gradient(90deg, #00c9c8, #00e87a);
  z-index: 9999;
  width: 0%;
  transition: width .1s linear;
`;
document.body.prepend(progressBar);

window.addEventListener('scroll', () => {
  const scrollTop = window.scrollY;
  const docHeight = document.documentElement.scrollHeight - window.innerHeight;
  const pct = (scrollTop / docHeight) * 100;
  progressBar.style.width = pct + '%';
});

/* ---------- 23. INIT ---------- */
// Make sure GSAP refreshes after page load
window.addEventListener('load', () => {
  ScrollTrigger.refresh();
});