let translations = {};
let lang = localStorage.getItem("lang") || "pl";

function loadLang() {
    fetch(`${window.location.origin}${window.location.pathname.includes("/SzachUZ") ? "/SzachUZ" : ""}/i18n/${lang}.json`)
        .then(r => r.json())
        .then(data => {
            translations = data;
            applyTranslations();
        });
}

function setLang(l) {
    lang = l;
    localStorage.setItem("lang", l);
    loadLang();
}

function applyTranslations() {
    document.querySelectorAll("[data-i18n]").forEach(el => {
        el.innerText = translations[el.dataset.i18n] || el.dataset.i18n;
    });

    document.querySelectorAll("[data-i18n-placeholder]").forEach(el => {
        el.placeholder =
            translations[el.dataset.i18n-placeholder] || el.placeholder;
    });
}

document.addEventListener("DOMContentLoaded", loadLang);
