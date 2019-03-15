<!DOCTYPE html>
<html lang="en">
<head>
    <title>Elements of Plumbing</title>
</head>
<body>
    <pre id="book"></pre>
    <script>
        const es = new EventSource("/eventstream");
        const book = document.getElementById('book');
        es.onmessage = ev => book.innerHTML += ev.data + '\n';
    </script>
</body>
</html>