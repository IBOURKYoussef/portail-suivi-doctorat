package ma.spring.userservice.dto;

@lombok.AllArgsConstructor
@lombok.Getter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}