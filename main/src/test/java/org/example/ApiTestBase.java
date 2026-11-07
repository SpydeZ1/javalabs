package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.model.UserStatus;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {

    static final String PASSWORD = "Str0ng#Passw0rd";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected record Account(long id, String username) {
    }

    protected Account account(UserRole role) {
        return account(role, UserStatus.ACTIVE);
    }

    protected Account account(UserRole role, UserStatus status) {
        String name = unique(role.name().toLowerCase());
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@studio.local");
        user.setRole(role);
        user.setStatus(status);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        User saved = userRepository.save(user);
        return new Account(saved.getId(), name);
    }

    protected MockHttpServletRequestBuilder as(Account account, MockHttpServletRequestBuilder builder) {
        return builder.with(httpBasic(account.username(), PASSWORD)).with(csrf());
    }

    protected MockHttpServletRequestBuilder withoutCsrf(Account account, MockHttpServletRequestBuilder builder) {
        return builder.with(httpBasic(account.username(), PASSWORD));
    }

    protected long createProject(Account actor, long ownerId) throws Exception {
        MvcResult result = mockMvc.perform(as(actor, post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(ownerId))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    protected long createSegment(Account actor, long projectId, int sequenceNumber) throws Exception {
        MvcResult result = mockMvc.perform(as(actor, post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Сохранить","sequenceNumber":%d}
                                """.formatted(projectId, sequenceNumber))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    protected void startProject(Account actor, long projectId) throws Exception {
        mockMvc.perform(as(actor, post("/api/translation-projects/%d/start".formatted(projectId))))
                .andExpect(status().isOk());
    }

    protected MvcResult createTranslationRaw(Account actor, long segmentId, long authorId, String text) throws Exception {
        return mockMvc.perform(as(actor, post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"%s"}
                                """.formatted(segmentId, authorId, text))))
                .andReturn();
    }

    protected long createTranslation(Account actor, long segmentId, long authorId, String text) throws Exception {
        MvcResult result = createTranslationRaw(actor, segmentId, authorId, text);
        if (result.getResponse().getStatus() != 201) {
            throw new AssertionError("Ожидался статус 201, получен " + result.getResponse().getStatus());
        }
        return id(result);
    }

    protected void submit(Account actor, long translationId) throws Exception {
        mockMvc.perform(as(actor, post("/api/translations/%d/submit".formatted(translationId))))
                .andExpect(status().isOk());
    }

    protected void approve(Account actor, long translationId) throws Exception {
        mockMvc.perform(as(actor, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isOk());
    }

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected long id(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("id").asLong();
    }
}
