package org.example.cloudservice.integration;

import org.example.cloudservice.AbstractIntegrationTest;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.dto.LoginRequestDto;
import org.example.cloudservice.dto.LoginResponseDto;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class CloudServiceFlowIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserEntityRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/cloud";

        // reset & seed a test user
        userRepo.deleteAll();
        UserEntity u = new UserEntity();
        u.setUsername("testuser");
        u.setPassword(passwordEncoder.encode("password"));
        userRepo.save(u);

        // log in and grab the token
        ResponseEntity<LoginResponseDto> login = restTemplate.postForEntity(
                baseUrl + "/login",
                new LoginRequestDto("testuser", "password"),
                LoginResponseDto.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = login.getBody().getAuthToken();

        headers = new HttpHeaders();
        headers.set("auth-token", "Bearer " + token);
    }

    @Test
    void fullHappyPath() {
        // 1) UPLOAD
        MultiValueMap<String,Object> uploadBody = new LinkedMultiValueMap<>();
        uploadBody.add("file", new ClassPathResource("hello.txt"));
        uploadBody.add("hash", "unusedHash");
        HttpEntity<MultiValueMap<String,Object>> uploadReq =
                new HttpEntity<>(uploadBody, headers);

        ResponseEntity<Void> uploadResp = restTemplate.postForEntity(
                baseUrl + "/file?filename=hello.txt",
                uploadReq, Void.class
        );
        assertThat(uploadResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2) LIST → 1 entry
        HttpEntity<Void> listReq = new HttpEntity<>(headers);
        ResponseEntity<FileDto[]> listResp = restTemplate.exchange(
                baseUrl + "/list?limit=10",
                HttpMethod.GET, listReq,
                FileDto[].class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody())
                .hasSize(1)
                .allSatisfy(dto -> assertThat(dto.getFilename()).isEqualTo("hello.txt"));

        // 3) DOWNLOAD → 200 + multipart
        ResponseEntity<String> dlResp = restTemplate.exchange(
                baseUrl + "/file?filename=hello.txt",
                HttpMethod.GET, new HttpEntity<>(headers),
                String.class
        );
        assertThat(dlResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dlResp.getHeaders().getContentType().toString())
                .startsWith("multipart/form-data");

        // 4) DELETE
        ResponseEntity<Void> delResp = restTemplate.exchange(
                baseUrl + "/file?filename=hello.txt",
                HttpMethod.DELETE, new HttpEntity<>(headers),
                Void.class
        );
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 5) LIST AGAIN → empty
        ResponseEntity<FileDto[]> listAgain = restTemplate.exchange(
                baseUrl + "/list?limit=10",
                HttpMethod.GET, listReq,
                FileDto[].class
        );
        assertThat(listAgain.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listAgain.getBody()).isEmpty();

        // 6) LOGOUT
        ResponseEntity<Void> logoutResp = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST, new HttpEntity<>(headers),
                Void.class
        );
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
