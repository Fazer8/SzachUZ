<%@ tag language="java" pageEncoding="UTF-8" %>
<ol id="leaderboard">
    <li>Fetching data...</li>
</ol>
<style>
    li div {
        display: flex;
        flex-direction: row;
        padding: 0 !important;
    }
    li div span {
        width: 4ch;
        padding-right: 2ch;
    }
</style>
<script>
async function loadTopTen() {
    try {
        const response = await fetch('/api/topten');
        if (!response.ok) {
            throw new Error(`HTTP error ${response.status}`);
        }

        const data = await response.json();
        const list = document.getElementById('leaderboard');

        list.innerHTML = '';

        data.forEach(item => {
            const div = document.createElement('div');
            const li = document.createElement('li');
            const p = document.createElement('p');
            const span = document.createElement('span');

            span.textContent = item.mmr;
            p.textContent = item.username;

            div.appendChild(span);
            div.appendChild(p);
            li.appendChild(div);
            list.appendChild(li);
        });
    } catch (err) {
        console.error('Failed to load top ten:', err);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadTopTen();
    setInterval(loadTopTen, 60 * 1000);
});
</script>
