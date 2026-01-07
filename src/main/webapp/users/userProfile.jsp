<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="My Profile">
    <jsp:attribute name="head">
        <style>
            #avatarContainer {
                width: 150px;
                height: 150px;
                border: 2px dashed #aaa;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 10px;
                position: relative;
            }

            #avatarContainer img {
                max-width: 100%;
                max-height: 100%;
            }

            #avatarContainer.dragover {
                border-color: #00f;
            }

            /* --- Toast Notifications CSS (Prawy Dolny Róg) --- */
            #toast-container {
                position: fixed;
                bottom: 20px; /* ZMIANA: Przypinamy do dołu */
                right: 20px; /* Przypinamy do prawej */
                z-index: 9999;
                display: flex;
                flex-direction: column; /* Dymki układają się w pionie */
                gap: 10px; /* Odstęp między dymkami */
            }

            .toast {
                min-width: 250px;
                max-width: 350px;
                padding: 15px 20px;
                border-radius: 8px;
                color: #fff;
                font-family: sans-serif;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); /* Nieco mocniejszy cień dla lepszego kontrastu na dole */

                /* Animacja wejścia */
                opacity: 0;
                transform: translateX(100%); /* Wylatują z prawej strony */
                transition: all 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55); /* Przyjemny efekt "sprężynowania" */

                /* Formatowanie tekstu */
                white-space: pre-wrap;
                word-wrap: break-word;
                overflow-wrap: break-word;
            }

            .toast.visible {
                opacity: 1;
                transform: translateX(0);
            }

            /* Kolory bez zmian */
            .toast.success {
                background-color: #2ecc71;
                border-left: 5px solid #27ae60;
            }

            .toast.error {
                background-color: #e74c3c;
                border-left: 5px solid #c0392b;
            }

            .toast.info {
                background-color: #3498db;
                border-left: 5px solid #2980b9;
            }


            /* --- Avatar Section Styles --- */
            .profile-header {
                display: flex;
                align-items: center; /* Wyrównanie do środka w pionie */
                gap: 20px;
                margin-bottom: 20px;
            }

            #avatarImage {
                width: 100px;
                height: 100px;
                border-radius: 50%; /* Okrągły awatar */
                object-fit: cover;
                border: 3px solid #ddd;
            }

            .avatar-actions {
                display: flex;
                flex-direction: column;
                gap: 10px;
            }

            /* Strefa Drag & Drop (domyślnie ukryta) */
            #avatarUploadZone {
                display: none; /* Ukryte na start */
                margin-top: 10px;
                padding: 20px;
                border: 2px dashed #aaa;
                border-radius: 8px;
                text-align: center;
                background-color: #f9f9f9;
                cursor: pointer;
                transition: background 0.3s;
            }

            #avatarUploadZone.dragover {
                border-color: #3498db;
                background-color: #eaf6ff;
            }

            /* Przyciski */
            button {
                cursor: pointer;
                padding: 8px 15px;
            }

            .btn-primary {
                background-color: #3498db;
                color: white;
                border: none;
                border-radius: 4px;
            }

            .btn-danger {
                background-color: #e74c3c;
                color: white;
                border: none;
                border-radius: 4px;
            }
        </style>
        <script>
            const baseUrl = "${pageContext.request.contextPath}/api/profile";
            let currentAvatar = null;
            let currentUserData = {};

            // --- 1. System Powiadomień (Toast) ---
            function showToast(message, type = 'info') {
                let container = document.getElementById('toast-container');
                if (!container) {
                    container = document.createElement('div');
                    container.id = 'toast-container';
                    document.body.appendChild(container);
                }

                const toast = document.createElement('div');
                toast.className = `toast ${type}`;
                toast.textContent = message;

                container.appendChild(toast);

                // Animacja wejścia (małe opóźnienie żeby CSS zadziałał)
                requestAnimationFrame(() => {
                    toast.classList.add('visible');
                });

                // Usuwanie po 4 sekundach
                setTimeout(() => {
                    toast.classList.remove('visible');
                    setTimeout(() => toast.remove(), 300); // Czekamy aż zniknie animacja
                }, 4000);
            }

            // --- 3. Pobieranie Profilu ---
            async function getMyProfile() {
                try {
                    const response = await authFetch(baseUrl + "/me", {method: "GET"});
                    if (response.ok) {
                        currentUserData = await response.json();
                        currentAvatar = currentUserData.userAvatar || null;
                        renderProfile(currentUserData);
                    } else {
                        console.warn("Status:", response.status);
                        if (response.status === 401) showToast("Sesja wygasła. Zaloguj się ponownie.", "error");
                    }
                } catch (e) {
                    console.error("Error:", e);
                    showToast("Błąd połączenia z serwerem.", "error");
                }
            }

            function renderProfile(data) {
                document.getElementById("emailField").textContent = data.email || "(no email)";
                document.getElementById("usernameField").textContent = data.username || "(no username)";
                document.getElementById("languageField").textContent = data.language || "PL";
                document.getElementById("darkModeField").textContent = data.darkMode ? "ON" : "OFF";
                updateAvatarUI();
            }

            const DEFAULT_AVATAR_FILENAME = "default_avatar.png";

            // --- 4. Avatar UI ---
            function updateAvatarUI() {
                const img = document.getElementById("avatarImage");
                const toggleBtn = document.getElementById("toggleUploadBtn");
                const delBtn = document.getElementById("deleteAvatarBtn");
                const uploadZone = document.getElementById("avatarUploadZone");

                // 1. Ustawienie źródła obrazka
                // Jeśli currentAvatar jest null, używamy domyślnego
                const avatarFile = currentAvatar || DEFAULT_AVATAR_FILENAME;

                // Zabezpieczenie: jeśli avatarFile to "default.png" (stara nazwa) traktuj jak domyślny
                // Zakładamy, że backend serwuje pliki pod /api/profile/avatars/
                img.src = baseUrl + "/avatars/" + avatarFile;

                // 2. Sprawdzenie czy to domyślny awatar
                const isDefault = (avatarFile === DEFAULT_AVATAR_FILENAME || avatarFile === "default.png");

                // 3. Konfiguracja przycisku "Zmień / Dodaj"
                if (isDefault) {
                    toggleBtn.textContent = "Dodaj awatar";
                } else {
                    toggleBtn.textContent = "Zmień awatar";
                }

                // 4. Konfiguracja przycisku "Usuń"
                if (isDefault) {
                    delBtn.style.display = "none"; // Ukryj, jeśli domyślny
                } else {
                    delBtn.style.display = "block"; // Pokaż, jeśli własny
                    delBtn.textContent = "Usuń awatar";
                }

                // Ukryj strefę uploadu po odświeżeniu widoku
                uploadZone.style.display = "none";
            }

            function toggleUploadZone() {
                const zone = document.getElementById("avatarUploadZone");
                if (zone.style.display === "none" || zone.style.display === "") {
                    zone.style.display = "block";
                } else {
                    zone.style.display = "none";
                }
            }

            // --- 5. Akcje Użytkownika (z użyciem Toast) ---

            async function uploadAvatar(file) {
                if (!file.type.startsWith("image/")) {
                    showToast("To nie jest plik graficzny!", "error");
                    return;
                }
                const formData = new FormData();
                formData.append("file", file);
                formData.append("filename", file.name);

                try {
                    const res = await authFetch(baseUrl + "/me/avatar", {method: "PUT", body: formData});
                    if (res.ok) {
                        showToast("Awatar zaktualizowany pomyślnie!", "success");
                        getMyProfile();
                    } else {
                        const txt = await res.text();
                        showToast("Błąd: " + txt, "error");
                    }
                } catch (e) {
                    showToast("Błąd wysyłania pliku.", "error");
                }
            }

            async function deleteAvatar(e) {
                e.stopPropagation();
                if (!confirm("Czy na pewno usunąć awatar?")) return;

                const res = await authFetch(baseUrl + "/me/avatar", {method: "DELETE"});
                if (res.ok) {
                    showToast("Awatar usunięty.", "info");
                    getMyProfile();
                } else {
                    showToast("Nie udało się usunąć awatara.", "error");
                }
            }

            // Username
            function showUsernameInput() {
                document.getElementById("usernameSection").style.display = "inline-block";
            }

            async function updateUsername() {
                const newVal = document.getElementById("usernameInput").value;
                if (!newVal) {
                    showToast("Nazwa nie może być pusta!", "error");
                    return;
                }

                const res = await authFetch(baseUrl + "/me/username", {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({value: newVal})
                });

                if (res.ok) {
                    showToast("Nazwa użytkownika zmieniona!", "success");
                    document.getElementById("usernameSection").style.display = "none";
                    getMyProfile();
                } else showToast("Błąd zmiany nazwy.", "error");
            }

            // Language
            async function toggleLanguage() {
                const current = currentUserData.language || "PL";
                const nextLang = current === "PL" ? "EN" : "PL";

                const res = await authFetch(baseUrl + "/me/language", {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({value: nextLang})
                });
                if (res.ok) {
                    showToast("Język zmieniony na " + nextLang, "success");
                    getMyProfile();
                }
            }

            // Dark Mode
            async function toggleDarkMode() {
                const newMode = !(currentUserData.darkMode);
                const res = await authFetch(baseUrl + "/me/darkMode", {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({darkMode: newMode})
                });
                if (res.ok) {
                    showToast("Tryb ciemny: " + (newMode ? "Włączony" : "Wyłączony"), "info");
                    const newTheme = newMode ? "light" : "dark";
                    localStorage.setItem("theme", newTheme);
                    document.documentElement.classList.toggle("dark", newTheme === "dark");
                    getMyProfile();
                }
            }

            // Password
            function showPasswordInput() {
                document.getElementById("passwordSection").style.display = "block";
            }

            async function updatePassword() {
                const oldPass = document.getElementById("oldPasswordInput").value;
                const newPass = document.getElementById("newPasswordInput").value;
                const confirmPass = document.getElementById("confirmPasswordInput").value;

                if (newPass !== confirmPass) {
                    showToast("Hasła nie są identyczne!", "error");
                    return;
                }

                const res = await authFetch(baseUrl + "/me/password", {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({oldPassword: oldPass, newPassword: newPass})
                });

                if (res.ok) {
                    showToast("Hasło zostało zmienione pomyślnie.", "success");
                    document.getElementById("passwordSection").style.display = "none";
                    document.getElementById("oldPasswordInput").value = "";
                    document.getElementById("newPasswordInput").value = "";
                    document.getElementById("confirmPasswordInput").value = "";
                } else {
                    const txt = await res.text();
                    // Tutaj txt to np. "Incorrect old password."
                    showToast(txt, "error");
                }
            }

            // Init
            window.addEventListener("DOMContentLoaded", () => {
                const zone = document.getElementById("avatarUploadZone");
                const input = document.getElementById("avatarInput");

                // Kliknięcie w strefę otwiera wybór plików
                zone.addEventListener("click", () => input.click());

                // Drag & Drop
                zone.addEventListener("dragover", (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    zone.classList.add("dragover");
                });

                zone.addEventListener("dragleave", (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    zone.classList.remove("dragover");
                });

                zone.addEventListener("drop", (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    zone.classList.remove("dragover");
                    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                        uploadAvatar(e.dataTransfer.files[0]);
                    }
                });

                // Input change
                input.addEventListener("change", (e) => {
                    if (e.target.files && e.target.files[0]) {
                        uploadAvatar(e.target.files[0]);
                    }
                });

                // Pobierz dane na start
                getMyProfile();
            });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body">
        <main class="site-margin">
            <div class="profile-header">
                <img id="avatarImage" src="" alt="Avatar"/>

                <div class="avatar-actions">
                    <button id="toggleUploadBtn" class="btn-primary" onclick="toggleUploadZone()">
                        Zmień awatar
                    </button>

                    <button id="deleteAvatarBtn" class="btn-danger" onclick="deleteAvatar()" style="display:none;">
                        Usuń awatar
                    </button>
                </div>
            </div>

            <div id="avatarUploadZone">
                <p>Przeciągnij zdjęcie tutaj lub kliknij, aby wybrać</p>
                <input type="file" id="avatarInput" style="display:none;" accept="image/*"/>
            </div>

            <div>
                <p>Email: <span id="emailField"></span></p>
                <p>
                    Username: <span id="usernameField"></span>
                    <button onclick="showUsernameInput()">Update Username</button>
                    <span id="usernameResult"></span>
                    <span id="usernameSection" style="display:none;">
                        <input type="text" id="usernameInput" placeholder="New username"/>
                        <button onclick="updateUsername()">Confirm</button>
                    </span>
                </p>
                <p>
                    Language: <span id="languageField"></span>
                    <button id="languageButton" onclick="toggleLanguage()">Language</button>
                    <span id="languageResult"></span>
                </p>
                <p>
                    Dark Mode: <span id="darkModeField"></span>
                    <button id="darkModeButton" onclick="toggleDarkMode()">Dark Mode</button>
                    <span id="darkModeResult"></span>
                </p>
                <p>
                    Password: ********
                    <button onclick="showPasswordInput()">Update Password</button>
                    <span id="passwordResult"></span>
                    <span id="passwordSection" style="display:none;">
                        <input type="password" id="oldPasswordInput" placeholder="Old password"/>
                        <input type="password" id="newPasswordInput" placeholder="New password"/>
                        <input type="password" id="confirmPasswordInput" placeholder="Confirm new password"/>
                        <button onclick="updatePassword()">Confirm</button>
                    </span>
                </p>
            </div>
            <pre id="profileResult"></pre>
        </main>
    </jsp:attribute>
</t:layout>
