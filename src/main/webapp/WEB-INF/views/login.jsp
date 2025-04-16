<%@ page import="java.util.Objects" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
</head>
<div class="container">
    <h2>Login</h2>

    <form action="/login" method="post">
        <label for="username">Username:</label>
        <input type="text" id="username" name="username" required><br><br>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>
        <c:if test="${param.error ne null}">
            <p style="color:red;">Invalid username or password.</p>
        </c:if>
        <c:if test="${param.logout ne null}">
            <p style="color:green;">You have been logged out.</p>
        </c:if>
        <input type="submit" value="Login">
    </form>
</div>
</html>