import java.net.*;
import java.io.*;
import java.util.*;

public class test_login_dashboard {
    public static void main(String[] args) throws Exception {
        String loginUrl = "http://localhost:8084/login";
        String dashboardUrl = "http://localhost:8084/dashboard";
        
        CookieManager cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cm);
        
        // Login
        System.out.println("=== Logging in ===");
        String postData = "username=admin&password=admin123";
       URLConnection conn = new URL(loginUrl).openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes());
        }
        
        int loginResponseCode = ((HttpURLConnection) conn).getResponseCode();
        System.out.println("Login Response Code: " + loginResponseCode);
        
        // Check cookies
        System.out.println("\n=== Cookies after login ===");
        CookieStore cookies = cm.getCookieStore();
        List<HttpCookie> cookieList = cookies.getCookies();
        for (HttpCookie cookie : cookieList) {
            System.out.println(cookie.getName() + " = " + cookie.getValue());
        }
        
        // Access dashboard
        System.out.println("\n=== Accessing Dashboard ===");
        URLConnection dashboardConn = new URL(dashboardUrl).openConnection();
        dashboardConn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        int dashboardResponseCode = ((HttpURLConnection) dashboardConn).getResponseCode();
        System.out.println("Dashboard Response Code: " + dashboardResponseCode);
        
        // Read dashboard content
        if (dashboardResponseCode == 200) {
            System.out.println("\n=== Dashboard Content (first 500 chars) ===");
            BufferedReader in = new BufferedReader(new InputStreamReader(dashboardConn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            int count = 0;
            while ((line = in.readLine()) != null && count < 20) {
                content.append(line).append("\n");
                count++;
            }
            in.close();
            
            String body = content.toString();
            System.out.println(body.substring(0, Math.min(500, body.length())));
            
            // Check for title
            if (body.contains("<title>")) {
                System.out.println("\n✓ Template rendered successfully!");
            } else {
                System.out.println("\n✗ Template may have issues");
            }
        } else {
            System.out.println("Failed to access dashboard");
            BufferedReader in = new BufferedReader(new InputStreamReader(((HttpURLConnection) dashboardConn).getErrorStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        }
    }
}
