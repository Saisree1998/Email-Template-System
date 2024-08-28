<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Email Template System</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap" rel="stylesheet">
    <style>

    body {
    font-family: 'Roboto', sans-serif; 
    margin: 0;
    padding: 0;
    background: url('images/login_img.jpeg') no-repeat center center fixed; 
    background-size: cover;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
}

.container {
    max-width: 400px;
    background: white;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

h1 {
    text-align: center;
    color: #242f60;
    margin-bottom: 20px;
    font-family: 'Jost', sans-serif;
}

.error {
    color: #dc3545;
    text-align: center;
    margin-bottom: 15px;
}

label {
    display: block;
    margin: 10px 0 5px;
    font-weight: bold;
    color: #343a40;
}

input {
    width: 100%;
    padding: 12px;
    margin-bottom: 15px;
    border: 1px solid #ced4da;
    border-radius: 5px;
    box-sizing: border-box;
    transition: border-color 0.3s;
}

input:focus {
    border-color: #242f60;
    outline: none;
}

button {
    width: 100%;
    padding: 12px;
    background-color: #242f60;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s;
}

button:hover {
    background-color: #242f60;
}

.footer {
    text-align: center;
    margin-top: 20px;
    font-size: 0.9em;
    color: #666;
}

.footer a {
    color: #242f60;
    text-decoration: none;
}

.footer a:hover {
    text-decoration: underline;
}

    </style>
</head>
<body>
    <div class="container">
        <h1>Login</h1>
        <c:if test="${not empty errorMessage}">
            <div class="error">${errorMessage}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login"> 
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Login</button>
        </form>
        <div class="footer">
            <p><a href="index.html">Back to Home</a></p>
        </div>
    </div>
</body>
</html>
