package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;

public class GenerateEmailServlet extends HttpServlet {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.compose");
    private static final String USER_ID = "user"; 

    // Environment variables
    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    private static final String APPLICATION_NAME = System.getenv("GOOGLE_APPLICATION_NAME") != null ? 
        System.getenv("GOOGLE_APPLICATION_NAME") : "Email Draft Generator";
    private static final String REDIRECT_URI = System.getenv("GOOGLE_REDIRECT_URI") != null ? 
        System.getenv("GOOGLE_REDIRECT_URI") : "http://localhost:8080/TemplateSystem/generateEmail";

    private Gmail getGmailService(HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException {
        HttpSession session = request.getSession();
        Gmail service = (Gmail) session.getAttribute("gmailService");

        if (service != null) {
            return service; // Gmail service already exists in session
        }

        try {
            OAuthTokens tokens = DatabaseUtil.getOAuthTokens(USER_ID);
            if (tokens != null) {
                Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                        .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                        .setJsonFactory(JSON_FACTORY)
                        .setClientAuthentication(new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET))
                        .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                        .build()
                        .setAccessToken(tokens.getAccessToken())
                        .setRefreshToken(tokens.getRefreshToken())
                        .setExpirationTimeMilliseconds(tokens.getTokenExpiry().getTime());

                // Check if the token is expired and refresh if needed
                if (credential.getExpiresInSeconds() <= 60) {
                    try {
                        credential.refreshToken();
                        OAuthTokens newTokens = new OAuthTokens(
                                credential.getAccessToken(),
                                credential.getRefreshToken(),
                                new Timestamp(System.currentTimeMillis() + credential.getExpiresInSeconds() * 1000)
                        );
                        DatabaseUtil.saveOAuthTokens(USER_ID, newTokens.getAccessToken(), newTokens.getRefreshToken(), newTokens.getTokenExpiry());
                    } catch (IOException e) {
                        e.printStackTrace();
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to refresh token: " + e.getMessage());
                        return null;
                    }
                }

                service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                session.setAttribute("gmailService", service);
                return service;
            }

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                    .setAccessType("offline")
                    .build();

            Credential credential = flow.loadCredential(USER_ID);
            if (credential == null || credential.getExpiresInSeconds() <= 60) {
                String authorizationUrl = flow.newAuthorizationUrl()
                        .setRedirectUri(REDIRECT_URI)
                        .build();
                response.sendRedirect(authorizationUrl);
                return null; // User needs to authenticate
            }

            service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            session.setAttribute("gmailService", service);
            return service;
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred: " + e.getMessage());
            return null;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Security error occurred: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        if (code != null) {
            handleOAuthCallback(code, request, response);
            return;
        }

        response.sendRedirect("index.html");
    }

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("doPost invoked");

    // Retrieve parameters
    String studentId = request.getParameter("studentId");
    String studentFirstName = request.getParameter("studentFirstName");
    String studentLastName = request.getParameter("studentLastName");
    String studentEmail = request.getParameter("studentEmail"); // Retrieve the student's email

    String[] moduleTitles = request.getParameterValues("moduleTitle");
    String[] courseworkTitles = request.getParameterValues("courseworkTitle");
    String[] originalDeadlines = request.getParameterValues("originalDeadline");
    String[] requestedExtensions = request.getParameterValues("RequestedExtension");
    String[] decisions = request.getParameterValues("decision");
    String[] comments = request.getParameterValues("comments");

    // Debugging parameter retrieval
    System.out.println("Received Parameters: ");
    System.out.println("StudentId: " + studentId);
    System.out.println("First Name: " + studentFirstName);
    System.out.println("Last Name: " + studentLastName);
    System.out.println("Student Email: " + studentEmail); // Debugging student email

    // Validate required fields
    if (studentId == null || studentFirstName == null || studentLastName == null || studentEmail == null) {
        request.setAttribute("message", "Missing required student information.");
        request.getRequestDispatcher("template.jsp").forward(request, response);
        return; // Stop further processing
    }

    // Check module and coursework details
    if (moduleTitles != null && moduleTitles.length > 0) {
        for (int i = 0; i < moduleTitles.length; i++) {
            // Ensure all coursework details are available
            if (courseworkTitles != null && originalDeadlines != null && requestedExtensions != null && decisions != null && comments != null) {
                if (i < courseworkTitles.length && i < originalDeadlines.length && i < requestedExtensions.length &&
                        i < decisions.length && i < comments.length) {
                    System.out.println("Module Title: " + moduleTitles[i]);
                    System.out.println("Coursework Title: " + courseworkTitles[i]);
                    System.out.println("Original Deadline: " + originalDeadlines[i]);
                    System.out.println("Requested Extension: " + requestedExtensions[i]);
                    System.out.println("Decision: " + decisions[i]);
                    System.out.println("Comments: " + comments[i]);
                } else {
                    System.out.println("Mismatch in coursework details for index: " + i);
                }
            } else {
                System.out.println("One or more coursework detail arrays are null.");
            }
        }
    } else {
        System.out.println("No module titles provided.");
        request.setAttribute("message", "No module titles provided.");
        request.getRequestDispatcher("template.jsp").forward(request, response);
        return; // Stop further processing
    }

    try {
        Gmail service = getGmailService(request, response);
        if (service == null) {
            System.out.println("Gmail service is null, redirecting for authorization");
            request.setAttribute("message", "Gmail service is unavailable. Please authorize.");
            request.getRequestDispatcher("template.jsp").forward(request, response);
            return; // Stop further processing
        }

        // Generate the email content
        String emailContent = generateEmailContent(studentId, studentFirstName, studentLastName,
                moduleTitles, courseworkTitles, originalDeadlines, requestedExtensions, decisions, comments);

        // Save the email as a draft
        if (saveEmailDraftInGmail(service, studentEmail, emailContent)) {
            request.setAttribute("message", "Email draft successfully saved.");
        } else {
            request.setAttribute("message", "Failed to save email draft.");
        }

        request.getRequestDispatcher("template.jsp").forward(request, response);
    } catch (Exception e) {
        e.printStackTrace();
        request.setAttribute("message", "An error occurred while processing your request: " + e.getMessage());
        request.getRequestDispatcher("template.jsp").forward(request, response);
    }
}


    private void handleOAuthCallback(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                    .setAccessType("offline")
                    .build();

            TokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();

            flow.createAndStoreCredential(tokenResponse, USER_ID);

            OAuthTokens tokens = new OAuthTokens(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    new Timestamp(System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() * 1000)
            );
            DatabaseUtil.saveOAuthTokens(USER_ID, tokens.getAccessToken(), tokens.getRefreshToken(), tokens.getTokenExpiry());

            Credential credential = flow.loadCredential(USER_ID);
            Gmail service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            HttpSession session = request.getSession();
            session.setAttribute("gmailService", service);

            response.sendRedirect("template.jsp");
        } catch (IOException | SQLException | GeneralSecurityException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage());
        }
    }

    private String generateEmailContent(
        String studentId, 
        String studentFirstName, 
        String studentLastName, 
        String[] moduleTitles, 
        String[] courseworkTitles, 
        String[] courseworkOriginalDeadlines, 
        String[] courseworkRequestedExtensions, 
        String[] decisions, 
        String[] comments
    ) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("Dear ").append(studentFirstName)
            .append(" ").append(studentLastName)
            .append(" (").append(studentId).append("),\n\n");
        
        emailContent.append("Thank you for your application for an extension to your coursework deadlines due to extenuating circumstances. Below are the decisions for your requests:\n\n");

        for (int i = 0; i < courseworkTitles.length; i++) {
            emailContent.append("Module: ").append(moduleTitles[i])
                .append(", ").append(courseworkTitles[i]).append("\n")
                .append("Original Deadline: ").append(courseworkOriginalDeadlines[i]).append("\n")
                .append("Requested Extension: ").append(courseworkRequestedExtensions[i]).append("\n")
                .append("Decision: ").append(decisions[i]).append("\n")
                .append("Comments: ").append((comments[i] != null && !comments[i].isEmpty()) ? comments[i] : "None").append("\n\n");
        }

        emailContent.append("If you have any questions or require further assistance, please do not hesitate to reach out to us.\n\n")
            .append("Best regards,\nSwansea University,\n")
            .append("Email: extenuatingcircumstances@swansea.ac.uk\n");

        return emailContent.toString();
    }

    private boolean saveEmailDraftInGmail(Gmail service, String recipientEmail, String emailContent) {
        try {
            Message message = createMessageWithEmail(recipientEmail, emailContent);
            Draft draft = new Draft().setMessage(message);
            draft = service.users().drafts().create("me", draft).execute();
            System.out.println("Draft created with ID: " + draft.getId());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save draft: " + e.getMessage());
            return false;
        }
    }
    

    private Message createMessageWithEmail(String recipientEmail, String emailContent) throws IOException {
        String raw = String.format("From: extenuatingcircumstances@swansea.ac.uk\nTo: %s\nSubject: Update on Your Extenuating Circumstances Request\n\n%s",
                recipientEmail, emailContent);
        String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
    
}
