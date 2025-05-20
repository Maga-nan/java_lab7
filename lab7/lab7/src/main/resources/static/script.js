const apiUrl = '/api';

async function createUser() {
    const username = document.getElementById('username').value;
    const res = await fetch(`${apiUrl}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username })
    });
    if (res.ok) {
        alert(' User created!');
        loadUsers();
    } else {
        alert(' Failed to create user');
    }
}

async function loadUsers() {
    const res = await fetch(`${apiUrl}/users`);
    const users = await res.json();
    const table = document.getElementById('userTable');
    table.innerHTML = '';
    users.forEach(user => {
        table.innerHTML += `
      <tr>
        <td class="border p-2">${user.id}</td>
        <td class="border p-2">${user.username}</td>
        <td class="border p-2">
          <button onclick="loadHistory(${user.id})"
            class="text-blue-600 hover:underline">View History</button>
        </td>
      </tr>`;
    });
}

async function convertTimestamp() {
    const timestamp = document.getElementById('timestamp').value;
    const userId = document.getElementById('userIdForConversion').value;
    const res = await fetch(`${apiUrl}/date/convert?timestamp=${timestamp}&userId=${userId}`);
    const result = await res.json();
    alert(`🕓 Local: ${result.localTime}, GMT: ${result.gmtTime}`);
    loadHistory(userId);
}

async function loadHistory(userId) {
    const res = await fetch(`${apiUrl}/date/users/${userId}/requests`);
    const history = await res.json();
    const list = document.getElementById('historyList');
    list.innerHTML = '';
    history.forEach(item => {
        const li = document.createElement('li');
        li.className = "bg-gray-100 p-3 rounded-md border";
        li.textContent = `#${item.id} — ${item.localTime} (GMT: ${item.gmtTime})`;
        list.appendChild(li);
    });
}

window.addEventListener('DOMContentLoaded', loadUsers);
