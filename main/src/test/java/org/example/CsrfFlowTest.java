package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import org.example.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CsrfFlowTest extends ApiTestBase {

    @Test
    void csrfTokenFromEndpointWorksTogetherWithCookie() throws Exception {
        MvcResult tokenResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        JsonNode body = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String token = body.path("token").asText();
        Cookie cookie = tokenResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);

        String username = unique("flow");
        mockMvc.perform(post("/api/auth/register")
                        .cookie(new Cookie("XSRF-TOKEN", cookie.getValue()))
                        .header("X-XSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s"}
                                """.formatted(username, username, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @Test
    void csrfTokenSurvivesAuthenticatedRequests() throws Exception {
        MvcResult tokenResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(tokenResult.getResponse().getContentAsString()).path("token").asText();
        Cookie cookie = tokenResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        Account translator = account(UserRole.TRANSLATOR);

        MvcResult profile = mockMvc.perform(withoutCsrf(translator, get("/api/users/me"))
                        .cookie(new Cookie("XSRF-TOKEN", cookie.getValue()))
                        .header("X-XSRF-TOKEN", token))
                .andExpect(status().isOk())
                .andReturn();
        assertNull(profile.getResponse().getCookie("XSRF-TOKEN"));

        mockMvc.perform(withoutCsrf(translator, post("/api/translation-projects")
                        .cookie(new Cookie("XSRF-TOKEN", cookie.getValue()))
                        .header("X-XSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(translator.id()))))
                .andExpect(status().isCreated());
    }
}
