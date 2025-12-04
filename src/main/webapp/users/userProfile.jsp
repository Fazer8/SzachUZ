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
        </style>
        <script>
            const baseUrl = "${pageContext.request.contextPath}/api/profile";
            let currentAvatar = null;

            // --- Display header info ---
            function displayHeaderInfo(headerText) {
                let pre = document.getElementById("profileResult");
                if (!pre) {
                    pre = document.createElement("pre");
                    pre.id = "profileResult";
                    document.body.prepend(pre);
                }
                pre.textContent = headerText;
            }

            // --- Fetch wrapper z automatycznym JWT (localStorage) ---
            async function authFetch(url, options = {}) {
                options = { ...options };
                options.headers = { ...(options.headers || {}) };

                const token = localStorage.getItem("authToken");
                console.log("DEBUG TOKEN:", {
                    val: token,
                    type: typeof token,
                    len: token ? token.length : 0,
                    isTruthy: !!token
                });
                if (token) {

                    //   -----------------<Frame of Shame>--------------------
                    <%--|options.headers["Authorization"] = `Bearer ${token}`;|--%>
                    //   -----------------<SkurwielJebany>--------------------

                    options.headers["Authorization"] = "Bearer " + token;

                }

                displayHeaderInfo("Sending request to " + url + "\nAuthorization header: " + (options.headers["Authorization"] || "(none)"));

                return fetch(url, options);
            }

            // --- Profile fetch ---
            async function getMyProfile() {
                let data = {};
                try {
                    const response = await authFetch(baseUrl + "/me", { method: "GET" });

                    if (response.ok) {
                        data = await response.json();
                        currentAvatar = data.userAvatar || null;
                    } else {
                        console.warn("Could not fetch profile, status:", response.status);
                        currentAvatar = null;
                    }
                } catch (e) {
                    console.warn("Error fetching profile:", e);
                    currentAvatar = null;
                }

                document.getElementById("emailField").textContent = data.email || "(no email)";
                document.getElementById("usernameField").textContent = data.username || "(no username)";
                document.getElementById("languageField").textContent = data.language || "(no language)";
                document.getElementById("darkModeField").textContent = data.darkMode ?? "(unknown)";

                updateAvatarUI();
            }

            // --- Avatar UI ---
            function updateAvatarUI() {
                const container = document.getElementById("avatarContainer");
                container.innerHTML = "";

                if (currentAvatar) {
                    const img = document.createElement("img");
                    img.src = "/opt/szachuz/avatars/" + currentAvatar;
                    container.appendChild(img);

                    const editBtn = document.createElement("button");
                    editBtn.textContent = "Edit";
                    editBtn.onclick = () => document.getElementById("avatarInput").click();
                    container.appendChild(editBtn);

                    const delBtn = document.createElement("button");
                    delBtn.textContent = "Delete";
                    delBtn.onclick = deleteAvatar;
                    container.appendChild(delBtn);
                } else {
                    const placeholder = document.createElement("span");
                    placeholder.textContent = "No avatar (offline or not set)";
                    container.appendChild(placeholder);

                    const addBtn = document.createElement("button");
                    addBtn.textContent = "Add Avatar";
                    addBtn.onclick = () => document.getElementById("avatarInput").click();
                    container.appendChild(addBtn);
                }
            }

            // --- Drag & Drop ---
            window.addEventListener("DOMContentLoaded", () => {
                const container = document.getElementById("avatarContainer");

                container.addEventListener("dragover", (e) => {
                    e.preventDefault();
                    container.classList.add("dragover");
                });

                container.addEventListener("dragleave", (e) => {
                    container.classList.remove("dragover");
                });

                container.addEventListener("drop", async (e) => {
                    e.preventDefault();
                    container.classList.remove("dragover");
                    if (e.dataTransfer.files.length > 0) {
                        uploadAvatar(e.dataTransfer.files[0]);
                    }
                });

                document.getElementById("avatarInput").addEventListener("change", (e) => {
                    if (e.target.files.length > 0) uploadAvatar(e.target.files[0]);
                });
            });

            async function uploadAvatar(file) {
                if (!file.type.startsWith("image/")) {
                    alert("Only image files are allowed!");
                    return;
                }

                const formData = new FormData();
                formData.append("file", file);
                formData.append("filename", file.name);

                const response = await authFetch(baseUrl + "/me/avatar", { method: "PUT", body: formData });

                if (response.ok) {
                    alert("Avatar updated!");
                    getMyProfile();
                } else {
                    const text = await response.text();
                    alert("Error: " + text);
                }
            }

            async function deleteAvatar() {
                const response = await authFetch(baseUrl + "/me/avatar", { method: "DELETE" });
                if (response.ok) {
                    alert("Avatar reset to default.");
                    getMyProfile();
                } else {
                    const text = await response.text();
                    alert("Error: " + text);
                }
            }

            window.onload = getMyProfile;
        </script>
    </jsp:attribute>

    <jsp:attribute name="body">
        <main class="site-margin border-color border-radius">
            <div id="avatarContainer"></div>
            <input type="file" id="avatarInput" style="display:none;" accept="image/*"/>

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
