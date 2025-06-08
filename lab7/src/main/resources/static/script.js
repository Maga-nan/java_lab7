const usersList = document.getElementById('users-list');
const conversionsList = document.getElementById('conversions-list');
const loadUsersBtn = document.getElementById('load-users');
const loadConversionsBtn = document.getElementById('load-conversions');
const convertForm = document.getElementById('convert-form');
const resultDiv = document.getElementById('result');

const API_BASE = '/api';

loadUsersBtn.onclick = () => {
    fetch(`${API_BASE}/users`)
        .then(res => res.json())
        .then(users => {
            usersList.innerHTML = '';
            users.forEach(u => {
                const li = document.createElement('li');
                li.textContent = `ID: ${u.id}, Имя: ${u.username || u.name || 'N/A'}`;
                usersList.appendChild(li);
            });
        });
};

loadConversionsBtn.onclick = () => {
    fetch(`${API_BASE}/date/conversions`)
        .then(res => res.json())
        .then(conv => {
            conversionsList.innerHTML = '';
            conv.forEach(c => {
                const li = document.createElement('li');
                li.textContent = `ID: ${c.id}, Timestamp: ${c.timestamp}, User ID: ${c.userId || '-'}, Result: ${c.result || '-'}`;
                conversionsList.appendChild(li);
            });
        });
};

convertForm.onsubmit = (e) => {
    e.preventDefault();
    const timestamp = document.getElementById('timestamp').value;
    const userId = document.getElementById('userId').value;

    let url = `${API_BASE}/date/convert?timestamp=${timestamp}`;
    if (userId) url += `&userId=${userId}`;

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error('Ошибка конвертации');
            return res.json();
        })
        .then(data => {
            resultDiv.textContent = `Результат: ${data.result || JSON.stringify(data)}`;
            resultDiv.style.color = 'green';
        })
        .catch(err => {
            resultDiv.textContent = err.message;
            resultDiv.style.color = 'red';
        });
};
