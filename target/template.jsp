<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.List, java.util.ArrayList" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Template System - Generate Email</title>
    <style>
        body {
            font-family: Arial, sans-serif; 
            margin: 0;
            padding: 0;
            background-color: #f8f9fa; 
        }
        header {
            background-color: #0069d9; 
            color: white;
            padding: 30px 0;
            text-align: center;
            border-bottom: 4px solid #0056b3;
        }
        .container {
            max-width: 800px;
            margin: 20px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
        }
        h1 {
            margin: 0 0 10px;
            font-size: 2.5em;
        }
        h2, h3 {
            color: #343a40;
        }
        label {
            display: block;
            margin: 15px 0 5px;
            font-weight: bold;
        }
        input, textarea, select {
            width: 100%;
            padding: 12px;
            margin-bottom: 15px;
            border: 1px solid #ced4da;
            border-radius: 5px;
            box-sizing: border-box;
            transition: border-color 0.3s;
        }
        input:focus, textarea:focus, select:focus {
            border-color: #0069d9;
            outline: none;
        }
        button {
            padding: 12px 25px;
            background-color: #28a745;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s;
            width: 48%;
            margin: 5px 1%;
        }
        button:hover {
            background-color: #218838;
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
            margin-top: 30px;
            font-size: 0.9em;
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
        <h1>Email Template System</h1>
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
            <div>
                <label for="studentId">Student ID: <span class="required">*</span></label>
                <input type="text" id="studentId" name="studentId" required>
            </div>
            <div>
                <label for="studentFirstName">Student First Name: <span class="required">*</span></label>
                <input type="text" id="studentFirstName" name="studentFirstName" required readonly>
            </div>
            <div>
                <label for="studentLastName">Student Last Name: <span class="required">*</span></label>
                <input type="text" id="studentLastName" name="studentLastName" required readonly>
            </div>
            <div id="module-container">
                <h3>Module Details</h3>
                <div class="module-item">
                    <label for="moduleTitle">Module Title: <span class="required">*</span></label>
                    <select class="moduleTitle" name="moduleTitle" id="moduleTitle" required>
                        <option value="" disabled selected>Select Module</option>
                        <% for (int i = 0; i < moduleNames.size(); i++) { %>
                            <option value="<%= moduleNames.get(i) %>"><%= moduleNames.get(i) %></option>
                        <% } %>
                    </select>
                </div>
                
                <div>
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
                <div>
                    <label for="originalDeadline">Original Deadline: <span class="required">*</span></label>
                    <input type="date" class="originalDeadline" name="originalDeadline" required>
                </div>
                <div>
                    <label for="RequestedExtension">Requested Extension: <span class="required">*</span></label>
                    <input type="date" class="RequestedExtension" name="RequestedExtension" required>
                </div>
                <div>
                    <label for="decision">Decision: <span class="required">*</span></label>
                    <select class="decision" name="decision" required>
                        <option value="" disabled selected>Select Decision</option>
                        <option value="Approved">Approved</option>
                        <option value="Denied">Denied</option>
                    </select>
                </div>
                <div>
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
