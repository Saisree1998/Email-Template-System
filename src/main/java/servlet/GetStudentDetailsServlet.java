package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/GetStudentDetailsServlet")
public class GetStudentDetailsServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/email_system";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Sivasai@20";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String studentId = request.getParameter("studentId");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Initialize response object
        String jsonResponse = "{\"success\":false}";

        if (studentId != null && !studentId.trim().isEmpty()) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

                // Updated query to fetch first name, last name, and email
                String query = "SELECT first_name, last_name, email FROM students WHERE student_id = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, studentId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String email = rs.getString("email");

                    // Include email in the JSON response
                    jsonResponse = String.format("{\"success\":true, \"firstName\":\"%s\", \"lastName\":\"%s\", \"email\":\"%s\"}", firstName, lastName, email);
                } else {
                    jsonResponse = "{\"success\":false, \"message\":\"No student found with the provided ID.\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                jsonResponse = String.format("{\"success\":false, \"message\":\"An error occurred: %s\"}", e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
                try { if (pstmt != null) pstmt.close(); } catch (Exception e) { e.printStackTrace(); }
                try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        } else {
            jsonResponse = "{\"success\":false, \"message\":\"Student ID cannot be null or empty.\"}";
        }

        // Write the JSON response
        out.write(jsonResponse);
        out.close();
    }
}
