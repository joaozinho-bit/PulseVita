/* ============================================
   VitalSense — Premium Health Sensor Landing
   script.js
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {

    /* ---------- Loader ---------- */
    window.addEventListener('load', () => {
        const loader = document.getElementById('loader');
        setTimeout(() => loader.classList.add('hidden'), 600);
    });

    /* ---------- Register GSAP Plugins ---------- */
    if (window.gsap && window.ScrollTrigger) {
        gsap.registerPlugin(ScrollTrigger);
    }

    /* ---------- Lenis Smooth Scroll ---------- */
    let lenis = null;
    if (window.Lenis) {
        lenis = new Lenis({
            duration: 1.1,
            easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
            smoothWheel: true,
            smoothTouch: false,
        });
        // Sync Lenis with ScrollTrigger
        lenis.on('scroll', ScrollTrigger.update);
        gsap.ticker.add((time) => lenis.raf(time * 1000));
        gsap.ticker.lagSmoothing(0);

        // Smooth anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', (e) => {
                const targetId = anchor.getAttribute('href');
                if (targetId === '#' || targetId.length < 2) return;
                const target = document.querySelector(targetId);
                if (target) {
                    e.preventDefault();
                    lenis.scrollTo(target, { offset: -80, duration: 1.2 });
                    // Close mobile menu if open
                    navMenu.classList.remove('active');
                    navToggle.classList.remove('active');
                }
            });
        });
    }

    /* ---------- Custom Cursor ---------- */
    const cursor = document.getElementById('cursor');
    const follower = document.getElementById('cursorFollower');
    let mouseX = 0, mouseY = 0;
    let followerX = 0, followerY = 0;

    if (cursor && window.matchMedia('(min-width: 1025px)').matches) {
        window.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;
            cursor.style.transform = `translate(${mouseX}px, ${mouseY}px) translate(-50%, -50%)`;
        });

        // Follower easing
        const animateFollower = () => {
            followerX += (mouseX - followerX) * 0.15;
            followerY += (mouseY - followerY) * 0.15;
            follower.style.transform = `translate(${followerX}px, ${followerY}px) translate(-50%, -50%)`;
            requestAnimationFrame(animateFollower);
        };
        animateFollower();

        // Hover state on interactive elements
        document.querySelectorAll('a, button, .price-card, .benefit-card, .feature-card').forEach(el => {
            el.addEventListener('mouseenter', () => {
                cursor.classList.add('hover');
                follower.classList.add('hover');
            });
            el.addEventListener('mouseleave', () => {
                cursor.classList.remove('hover');
                follower.classList.remove('hover');
            });
        });
    }

    /* ---------- Scroll Progress Bar ---------- */
    const progressBar = document.getElementById('scrollProgress');
    const updateProgress = () => {
        const scrollTop = window.scrollY;
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        const progress = (scrollTop / docHeight) * 100;
        progressBar.style.width = progress + '%';
    };
    window.addEventListener('scroll', updateProgress, { passive: true });

    /* ---------- Nav Scroll State ---------- */
    const nav = document.getElementById('nav');
    const navToggle = document.getElementById('navToggle');
    const navMenu = document.querySelector('.nav-menu');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) nav.classList.add('scrolled');
        else nav.classList.remove('scrolled');
    }, { passive: true });

    navToggle.addEventListener('click', () => {
        navToggle.classList.toggle('active');
        navMenu.classList.toggle('active');
    });

    /* ---------- Hero Text Animation with SplitType ---------- */
    if (window.SplitType) {
        const heroTitle = document.querySelector('.hero-title');
        const split = new SplitType(heroTitle, { types: 'lines, words', tagName: 'span' });

        // Animate words in
        gsap.from(split.words, {
            yPercent: 110,
            opacity: 0,
            duration: 1,
            ease: 'power4.out',
            stagger: 0.08,
            delay: 0.8
        });

        // Animate other hero elements
        gsap.from('.hero-badge', { opacity: 0, y: 20, duration: 0.8, delay: 0.4 });
        gsap.from('.hero-subtitle', { opacity: 0, y: 20, duration: 0.8, delay: 1.2 });
        gsap.from('.hero-cta .btn', { opacity: 0, y: 20, duration: 0.6, delay: 1.4, stagger: 0.1 });
        gsap.from('.hero-stats .stat', { opacity: 0, y: 20, duration: 0.6, delay: 1.6, stagger: 0.1 });
        gsap.from('.hero-marquee', { opacity: 0, duration: 1, delay: 2 });
    }

    /* ---------- Hero Product Float & Mouse Parallax ---------- */
    const heroProduct = document.getElementById('heroProduct');
    const heroVisual = document.getElementById('heroVisual');

    if (heroProduct) {
        // Continuous float
        gsap.to(heroProduct, {
            y: -20,
            duration: 3,
            repeat: -1,
            yoyo: true,
            ease: 'sine.inOut'
        });

        // Mouse parallax
        if (window.matchMedia('(min-width: 1025px)').matches) {
            heroVisual.addEventListener('mousemove', (e) => {
                const rect = heroVisual.getBoundingClientRect();
                const x = ((e.clientX - rect.left) / rect.width - 0.5) * 30;
                const y = ((e.clientY - rect.top) / rect.height - 0.5) * 30;
                gsap.to(heroProduct, { x: x, y: y - 20, duration: 1, ease: 'power2.out' });
            });
            heroVisual.addEventListener('mouseleave', () => {
                gsap.to(heroProduct, { x: 0, y: -20, duration: 1, ease: 'power2.out' });
            });
        }
    }

    /* ---------- Hero Floating Tags Animation ---------- */
    gsap.utils.toArray('.floating-tag').forEach((tag, i) => {
        gsap.to(tag, {
            y: i % 2 === 0 ? -10 : 10,
            duration: 2.5 + i * 0.3,
            repeat: -1,
            yoyo: true,
            ease: 'sine.inOut',
            delay: i * 0.2
        });
    });

    /* ---------- Reveal Animations on Scroll ---------- */
    gsap.utils.toArray('.reveal').forEach((el) => {
        gsap.fromTo(el,
            { opacity: 0, y: 40 },
            {
                opacity: 1, y: 0, duration: 1, ease: 'power3.out',
                scrollTrigger: {
                    trigger: el,
                    start: 'top 85%',
                    toggleActions: 'play none none none'
                }
            }
        );
    });

    /* ---------- Section Title Split Animation ---------- */
    if (window.SplitType) {
        document.querySelectorAll('.section-title').forEach(title => {
            const splitTitle = new SplitType(title, { types: 'lines', tagName: 'span' });
            gsap.fromTo(splitTitle.lines,
                { yPercent: 100, opacity: 0 },
                {
                    yPercent: 0, opacity: 1, duration: 1, ease: 'power3.out', stagger: 0.1,
                    scrollTrigger: {
                        trigger: title,
                        start: 'top 85%',
                        toggleActions: 'play none none none'
                    }
                }
            );
        });
    }

    /* ---------- Benefits Cards Stagger ---------- */
    gsap.fromTo('.benefit-card',
        { opacity: 0, y: 40 },
        {
            opacity: 1, y: 0, duration: 0.8, ease: 'power3.out', stagger: 0.1,
            scrollTrigger: {
                trigger: '.benefits-grid',
                start: 'top 80%',
                toggleActions: 'play none none none'
            }
        }
    );

    /* ---------- How It Works Steps ---------- */
    gsap.fromTo('.how-step',
        { opacity: 0, y: 50 },
        {
            opacity: 1, y: 0, duration: 0.8, ease: 'power3.out', stagger: 0.15,
            scrollTrigger: {
                trigger: '.how-grid',
                start: 'top 80%',
                toggleActions: 'play none none none'
            }
        }
    );

    // Animate the line fill
    const howLineFill = document.getElementById('howLineFill');
    if (howLineFill) {
        ScrollTrigger.create({
            trigger: '.how-grid',
            start: 'top 80%',
            end: 'bottom 50%',
            onUpdate: (self) => {
                howLineFill.style.width = (self.progress * 100) + '%';
            }
        });
    }

    /* ---------- Features Bento Stagger ---------- */
    gsap.fromTo('.feature-card',
        { opacity: 0, y: 50, scale: 0.97 },
        {
            opacity: 1, y: 0, scale: 1, duration: 1, ease: 'power3.out', stagger: 0.1,
            scrollTrigger: {
                trigger: '.features-bento',
                start: 'top 80%',
                toggleActions: 'play none none none'
            }
        }
    );

    /* ---------- Showcase Product Parallax & Rotation ---------- */
    const showcaseProduct = document.getElementById('showcaseProduct');
    const showcaseVisual = document.getElementById('showcaseVisual');

    if (showcaseProduct && showcaseVisual) {
        // Subtle scroll rotation
        gsap.to(showcaseProduct, {
            rotation: 8,
            scrollTrigger: {
                trigger: showcaseVisual,
                start: 'top bottom',
                end: 'bottom top',
                scrub: 1.5
            }
        });

        // Scale up on enter
        gsap.fromTo(showcaseProduct,
            { scale: 0.85, opacity: 0 },
            {
                scale: 1, opacity: 1, duration: 1.4, ease: 'power3.out',
                scrollTrigger: {
                    trigger: showcaseVisual,
                    start: 'top 75%',
                    toggleActions: 'play none none none'
                }
            }
        );

        // Floating animation
        gsap.to(showcaseProduct, {
            y: -15,
            duration: 3,
            repeat: -1,
            yoyo: true,
            ease: 'sine.inOut'
        });
    }

    /* ---------- Showcase Labels Reveal ---------- */
    const showcaseLabels = document.querySelectorAll('.showcase-label');
    showcaseLabels.forEach((label, i) => {
        ScrollTrigger.create({
            trigger: showcaseVisual,
            start: 'top 60%',
            onEnter: () => {
                gsap.to(label, {
                    opacity: 1,
                    duration: 0.6,
                    delay: 0.3 + i * 0.15,
                    ease: 'power2.out'
                });
                label.classList.add('visible');
            }
        });
    });

    /* ---------- Pricing Cards Stagger ---------- */
    gsap.fromTo('.price-card',
        { opacity: 0, y: 60 },
        {
            opacity: 1, y: 0, duration: 1, ease: 'power3.out', stagger: 0.15,
            scrollTrigger: {
                trigger: '.pricing-grid',
                start: 'top 80%',
                toggleActions: 'play none none none'
            }
        }
    );

    // Featured card gentle pulse
    gsap.to('.price-featured', {
        boxShadow: '0 30px 90px -20px rgba(0, 229, 255, 0.5)',
        duration: 2,
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut'
    });

    /* ---------- Trust Image Reveal ---------- */
    gsap.fromTo('.trust-image-wrap',
        { opacity: 0, scale: 0.95 },
        {
            opacity: 1, scale: 1, duration: 1.2, ease: 'power3.out',
            scrollTrigger: {
                trigger: '.trust-visual',
                start: 'top 75%',
                toggleActions: 'play none none none'
            }
        }
    );

    /* ---------- FAQ Accordion ---------- */
    const faqItems = document.querySelectorAll('.faq-item');
    faqItems.forEach(item => {
        const question = item.querySelector('.faq-question');
        const answer = item.querySelector('.faq-answer');

        question.addEventListener('click', () => {
            const isActive = item.classList.contains('active');

            // Close all
            faqItems.forEach(otherItem => {
                otherItem.classList.remove('active');
                const otherAnswer = otherItem.querySelector('.faq-answer');
                otherAnswer.style.maxHeight = null;
            });

            // Open clicked
            if (!isActive) {
                item.classList.add('active');
                answer.style.maxHeight = answer.scrollHeight + 'px';
            }
        });
    });

    /* ---------- Animated Counters ---------- */
    const counters = document.querySelectorAll('.stat-value');
    counters.forEach(counter => {
        const target = parseFloat(counter.dataset.target);
        const suffix = counter.dataset.suffix || '';
        const isDecimal = target % 1 !== 0;
        const obj = { val: 0 };

        ScrollTrigger.create({
            trigger: counter,
            start: 'top 85%',
            once: true,
            onEnter: () => {
                gsap.to(obj, {
                    val: target,
                    duration: 2,
                    ease: 'power2.out',
                    onUpdate: () => {
                        const v = obj.val;
                        counter.textContent = (isDecimal ? v.toFixed(1) : Math.floor(v)) + suffix;
                    }
                });
            }
        });
    });

    /* ---------- Tilt Effect on Cards ---------- */
    const tiltCards = document.querySelectorAll('[data-tilt]');
    if (window.matchMedia('(min-width: 1025px)').matches) {
        tiltCards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                const rect = card.getBoundingClientRect();
                const x = (e.clientX - rect.left) / rect.width - 0.5;
                const y = (e.clientY - rect.top) / rect.height - 0.5;
                gsap.to(card, {
                    rotationY: x * 8,
                    rotationX: -y * 8,
                    transformPerspective: 1000,
                    duration: 0.4,
                    ease: 'power2.out'
                });
            });
            card.addEventListener('mouseleave', () => {
                gsap.to(card, {
                    rotationY: 0,
                    rotationX: 0,
                    duration: 0.6,
                    ease: 'power3.out'
                });
            });
        });
    }

    /* ---------- Magnetic Buttons ---------- */
    const magneticButtons = document.querySelectorAll('.magnetic');
    if (window.matchMedia('(min-width: 1025px)').matches) {
        magneticButtons.forEach(btn => {
            btn.addEventListener('mousemove', (e) => {
                const rect = btn.getBoundingClientRect();
                const x = e.clientX - rect.left - rect.width / 2;
                const y = e.clientY - rect.top - rect.height / 2;
                gsap.to(btn, { x: x * 0.3, y: y * 0.4, duration: 0.6, ease: 'power3.out' });
            });
            btn.addEventListener('mouseleave', () => {
                gsap.to(btn, { x: 0, y: 0, duration: 0.6, ease: 'elastic.out(1, 0.4)' });
            });
        });
    }

    /* ---------- Hero Background Parallax ---------- */
    if (window.matchMedia('(min-width: 1025px)').matches) {
        const blobs = document.querySelectorAll('.blob');
        window.addEventListener('scroll', () => {
            const scrollY = window.scrollY;
            blobs.forEach((blob, i) => {
                const speed = (i + 1) * 0.15;
                gsap.to(blob, { y: scrollY * speed, duration: 0.5, ease: 'none' });
            });
        }, { passive: true });
    }

    /* ---------- Final CTA Parallax ---------- */
    const finalProduct = document.querySelector('.final-product');
    if (finalProduct) {
        gsap.to(finalProduct, {
            y: -60,
            scrollTrigger: {
                trigger: '.final-cta',
                start: 'top bottom',
                end: 'bottom top',
                scrub: 1.5
            }
        });
    }

    /* ---------- Refresh ScrollTrigger on load ---------- */
    window.addEventListener('load', () => {
        ScrollTrigger.refresh();
    });

    console.log('%cVitalSense landing loaded.', 'color: #00E5FF; font-family: monospace; font-size: 14px;');
});