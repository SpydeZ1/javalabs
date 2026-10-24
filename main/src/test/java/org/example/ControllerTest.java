package org.example;

import org.example.model.ProjectStatus;
import org.example.model.TranslationStatus;
import org.example.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crudAndWorkflowStayConsistent() throws Exception {
        long annaId = createUser("anna", "anna@studio.local", UserRole.TRANSLATOR);
        createUser("boris", "boris@studio.local", UserRole.EDITOR);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        MvcResult projectResult = mockMvc.perform(post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Кабинет","targetLanguage":"en","ownerId":%d}
                                """.formatted(annaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(ProjectStatus.DRAFT.name()))
                .andReturn();
        long projectId = id(projectResult);

        mockMvc.perform(post("/api/translation-projects/%d/start".formatted(projectId)))
                .andExpect(status().isConflict());

        MvcResult segmentResult = mockMvc.perform(post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Сохранить","sequenceNumber":1}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long segmentId = id(segmentResult);

        mockMvc.perform(post("/api/translation-projects/%d/start".formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ProjectStatus.IN_PROGRESS.name()));

        MvcResult translationResult = mockMvc.perform(post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"Save"}
                                """.formatted(segmentId, annaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(TranslationStatus.DRAFT.name()))
                .andReturn();
        long translationId = id(translationResult);

        mockMvc.perform(post("/api/translations/%d/approve".formatted(translationId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/translations/%d/submit".formatted(translationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TranslationStatus.SUBMITTED.name()));

        MvcResult changeResult = mockMvc.perform(post("/api/translations/%d/request-changes".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Короче\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translation.status").value(TranslationStatus.REJECTED.name()))
                .andExpect(jsonPath("$.comment.status").value("OPEN"))
                .andReturn();
        long commentId = objectMapper.readTree(changeResult.getResponse().getContentAsString())
                .path("comment").path("id").asLong();

        mockMvc.perform(post("/api/translations/%d/submit".formatted(translationId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/editorial-comments/%d/resolve".formatted(commentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(patch("/api/translations/%d".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Save changes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Save changes"))
                .andExpect(jsonPath("$.status").value(TranslationStatus.REJECTED.name()));

        mockMvc.perform(post("/api/translations/%d/submit".formatted(translationId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/translations/%d/approve".formatted(translationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TranslationStatus.APPROVED.name()));

        mockMvc.perform(post("/api/translation-projects/%d/complete".formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ProjectStatus.COMPLETED.name()));

        mockMvc.perform(post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Ещё","sequenceNumber":2}
                                """.formatted(projectId)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/users/%d".formatted(annaId)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/translation-projects/%d".formatted(projectId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/translation-projects/%d".formatted(projectId)))
                .andExpect(status().isNotFound());
    }

    private long createUser(String username, String email, UserRole role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","role":"%s"}
                                """.formatted(username, email, role.name())))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private long id(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("id").asLong();
    }
}
