import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class test_login {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Encoded: " + encoder.encode("admin123"));
        System.out.println("Matches: " + encoder.matches("admin123", "$2a$10$xxxxxxxx"));
    }
}