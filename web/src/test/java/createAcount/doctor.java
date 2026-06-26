package createAcount;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class doctor {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("Chuỗi hash cần dùng: " + hashedPassword);
    }
}
