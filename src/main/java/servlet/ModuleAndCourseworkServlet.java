package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/ModuleAndCourseworkServlet")
public class ModuleAndCourseworkServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/email_system";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Sivasai@20";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<String> moduleNames = new ArrayList<>();
        List<Integer> moduleIds = new ArrayList<>();
        List<String> courseworkTitles = new ArrayList<>();

        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            // Fetch Modules
            String sqlModules = "SELECT ModuleID, ModuleName FROM Modules";
            pstmt = conn.prepareStatement(sqlModules);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                moduleIds.add(rs.getInt("ModuleID"));
                moduleNames.add(rs.getString("ModuleName"));
            }
            rs.close();
            pstmt.close();

            // Fetch Assignments
            String sqlCoursework = "SELECT AssignmentTitle FROM Assignments";
            pstmt = conn.prepareStatement(sqlCoursework);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                courseworkTitles.add(rs.getString("AssignmentTitle"));
            }

            // Create JSON response
            JSONObject jsonResponse = new JSONObject();
            JSONArray modulesArray = new JSONArray();
            JSONArray courseworkArray = new JSONArray();

            for (int i = 0; i < moduleNames.size(); i++) {
                JSONObject module = new JSONObject();
                module.put("moduleId", moduleIds.get(i));
                module.put("moduleName", moduleNames.get(i));
                modulesArray.put(module);
            }

            for (String coursework : courseworkTitles) {
                courseworkArray.put(coursework);
            }

            jsonResponse.put("modules", modulesArray);
            jsonResponse.put("courseworkTitles", courseworkArray);
            jsonResponse.put("success", true);

            // Send JSON response
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            out.flush();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();

            // Handle error and send error response
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching module and coursework details: " + e.getMessage());

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(errorResponse.toString());
            out.flush();
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
