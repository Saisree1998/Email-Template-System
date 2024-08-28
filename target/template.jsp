<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.List, java.util.ArrayList" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Template System - Generate Email</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap" rel="stylesheet">
    <style>
        body {
    font-family: 'Roboto', sans-serif; 
    margin: 0;
    padding: 0;
    background-color: #f0f2f5;
    color: hsl(63, 37%, 20%);
}

header {
    background-color: #242f60; 
    color: white;
    padding: 60px 20px;
    text-align: center;
}

header h1 {
    font-family: 'Jost', sans-serif;
    font-size: 3em;
    margin: 0;
}

header p {
    font-size: 1.2em;
    margin-top: 10px;
}

.container {
    max-width: 800px; /* Compact container for better focus */
    margin: 40px auto;
    background: white;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
}

h1, h2, h3 {
    color: #242f60;
}

.form-group {
    display: flex;
    align-items: center;
    margin-bottom: 20px; /* Increased margin for better spacing */
}

.form-group label {
    flex: 0 0 180px; /* Fixed width for labels */
    margin-right: 15px;
    text-align: right;
    font-weight: bold;
    font-size: 1em; /* Standardized font size */
}

.form-group input,
.form-group textarea,
.form-group select {
    flex: 1;
    max-width: 500px; /* Adjusted max width for inputs */
    padding: 12px;
    border: 1px solid #ced4da;
    border-radius: 5px;
    box-sizing: border-box;
    transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
    border-color: #242f60;
    outline: none;
}

button {
    padding: 12px 24px;
    background-color: #242f60;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s;
    width: 48%;
    margin: 5px 1%;
    font-size: 1em; /* Uniform font size for buttons */
}

button:hover {
    background-color: #1b2a54;
}

.coursework-item {
    margin-bottom: 20px;
    padding: 20px;
    border: 1px solid #e9ecef;
    border-radius: 5px;
    background-color: #f1f3f5;
}

.required {
    color: #dc3545;
}

.template-overview {
    display: none;
    margin: 20px 0;
    padding: 15px;
    background-color: #e2e3e5;
    border: 1px solid #d3d3d3;
    border-radius: 5px;
}

.success-message {
    background-color: #d4edda;
    color: #155724;
    padding: 10px;
    border: 1px solid #c3e6cb;
    border-radius: 5px;
    margin-bottom: 20px;
}

.error-message {
    background-color: #f8d7da;
    color: #721c24;
    padding: 10px;
    border: 1px solid #f5c6cb;
    border-radius: 5px;
    margin-bottom: 20px;
}

footer {
    text-align: center;
    margin-top: 40px;
    padding: 20px 0;
    font-size: 0.9em;
    background-color: #242f60;
    color: #666;
}

.button-container {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
}


    </style>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="template.js" defer></script>
</head>
<body>
    <header>
        <img src="images/uni_logo.jpeg" alt="University Logo" style="width: 125px; height: auto; margin-bottom: 20px;" />
        <p>Generate emails with ease and precision</p>
    </header>

    <div class="container">
        <%
            String message = (String) request.getAttribute("message");
            if (message != null) {
                if (message.equals("Email draft created successfully.")) {
        %>
            <div class="success-message"><%= message %></div>
        <%
                } else {
        %>
            <div class="error-message"><%= message %></div>
        <%
                }
            }

            List<String> moduleNames = new ArrayList<>();
            List<Integer> moduleIds = new ArrayList<>();
            List<String> courseworkTitles = new ArrayList<>();

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/email_system", "root", "Sivasai@20");

                // Fetch modules
                String sqlModules = "SELECT ModuleID, ModuleName FROM Modules";
                pstmt = conn.prepareStatement(sqlModules);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    moduleIds.add(rs.getInt("ModuleID"));
                    moduleNames.add(rs.getString("ModuleName"));
                }

                // Fetch all coursework
                String sqlCoursework = "SELECT AssignmentTitle FROM Assignments";
                pstmt = conn.prepareStatement(sqlCoursework);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    courseworkTitles.add(rs.getString("AssignmentTitle"));
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {}
                if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                if (conn != null) try { conn.close(); } catch (SQLException e) {}
            }
        %>

        <form id="emailForm" method="post" action="/TemplateSystem/generateEmail">
            <div class="form-group">
                <label for="studentId">Student ID: <span class="required">*</span></label>
                <input type="text" id="studentId" name="studentId" required>
            </div>
            <div class="form-group">
                <label for="studentFirstName">Student First Name: <span class="required">*</span></label>
                <input type="text" id="studentFirstName" name="studentFirstName" required readonly>
            </div>
            <div class="form-group">
                <label for="studentLastName">Student Last Name: <span class="required">*</span></label>
                <input type="text" id="studentLastName" name="studentLastName" required readonly>
            </div>
            <div class="form-group">
                <label for="studentEmail">Student Email: <span class="required">*</span></label>
                <input type="email" id="studentEmail" name="studentEmail" required readonly>
            </div>            
            <div id="module-container">
                <h3>Module Details</h3>
                <div class="form-group">
                    <label for="moduleTitle">Module Title: <span class="required">*</span></label>
                    <select class="moduleTitle" name="moduleTitle" id="moduleTitle" required>
                        <option value="" disabled selected>Select Module</option>
                        <% for (int i = 0; i < moduleNames.size(); i++) { %>
                            <option value="<%= moduleNames.get(i) %>"><%= moduleNames.get(i) %></option>
                        <% } %>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="courseworkTitle">Select Coursework: <span class="required">*</span></label>
                    <select class="courseworkSelect" name="courseworkTitle" required>
                        <option value="" disabled selected>Select Coursework</option>
                        <%
                            for (String coursework : courseworkTitles) {
                        %>
                        <option value="<%= coursework %>"><%= coursework %></option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="originalDeadline">Original Deadline: <span class="required">*</span></label>
                    <input type="date" class="originalDeadline" name="originalDeadline" required>
                </div>
                <div class="form-group">
                    <label for="RequestedExtension">Requested Extension: <span class="required">*</span></label>
                    <input type="date" class="RequestedExtension" name="RequestedExtension" required>
                </div>
                <div class="form-group">
                    <label for="decision">Decision: <span class="required">*</span></label>
                    <select class="decision" name="decision" required>
                        <option value="" disabled selected>Select Decision</option>
                        <option value="Approved">Approved</option>
                        <option value="Denied">Denied</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="comments">Comments:</label>
                    <textarea class="comments" name="comments"></textarea>
                </div>
            </div>
            <div class="button-container">
                <button type="button" onclick="addModule()">Add Another Request</button>
                <button type="submit">Generate Email</button>
            </div>
        </form>
    </div>

    <footer>
        <p>&copy; 2024 Email Template System</p>
    </footer>
</body>
</html>
