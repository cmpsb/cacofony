<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>User-Agent Inspector</title>
        <style>
            html, body {
                height: 100%;
                background-color: beige;
            }
        </style>
    </head>
    <body>
        <h1>Welcome!</h1>
        <form method="POST">
            <label for="title">Title</label>
            <input type="text" name="title"><br>
            <textarea rows="24" cols="80" name="content"></textarea><br>
            <input type="checkbox" name="choose" value="One">The one<br>
            <input type="checkbox" name="choose" value="Other" checked="checked">The other<br>
            <select name="numbers" multiple>
                <option value="one">One</option>
                <option value="two">Two</option>
                <option value="three">Three</option>
                <option value="four">Four</option>
            </select><br>
            <input type="submit" name="submit" value="Submit">
        </form>
    </body>
</html>
