package ma.spring.userservice.dto;

<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
=======
@lombok.AllArgsConstructor
@lombok.Getter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
    }
}