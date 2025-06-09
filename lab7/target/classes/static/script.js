function convert() {
    const timestamp = document.getElementById('timestamp').value;
    const date = new Date(timestamp * 1000);
    document.getElementById('result').textContent = date.toLocaleString();
}