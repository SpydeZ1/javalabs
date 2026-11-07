package org.example;

import org.example.model.ProjectStatus;
import org.example.model.TranslationStatus;
import org.example.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllerTest extends ApiTestBase {

    @Test
    void crudAndWorkflowStayConsistent() throws Exception {
        Account admin = account(UserRole.ADMIN);
        Account anna = account(UserRole.TRANSLATOR);
        Account boris = account(UserRole.EDITOR);

        mockMvc.perform(as(admin, get("/api/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(as(admin, get("/api/users/999999")))
                .andExpect(status().isNotFound());

        mockMvc.perform(as(admin, post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s","role":"EDITOR"}
                                """.formatted(unique("created"), unique("created"), PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        MvcResult projectResult = mockMvc.perform(as(anna, post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Кабинет","targetLanguage":"en","ownerId":%d}
                                """.formatted(anna.id()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(ProjectStatus.DRAFT.name()))
                .andReturn();
        long projectId = id(projectResult);

        mockMvc.perform(as(anna, post("/api/translation-projects/%d/start".formatted(projectId))))
                .andExpect(status().isConflict());

        long segmentId = createSegment(anna, projectId, 1);

        mockMvc.perform(as(anna, post("/api/translation-projects/%d/start".formatted(projectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ProjectStatus.IN_PROGRESS.name()));

        MvcResult translationResult = createTranslationRaw(anna, segmentId, anna.id(), "Save");
        long translationId = id(translationResult);

        mockMvc.perform(as(boris, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isConflict());

        mockMvc.perform(as(anna, post("/api/translations/%d/submit".formatted(translationId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TranslationStatus.SUBMITTED.name()));

        MvcResult changeResult = mockMvc.perform(as(boris, post("/api/translations/%d/request-changes".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Короче\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translation.status").value(TranslationStatus.REJECTED.name()))
                .andExpect(jsonPath("$.comment.status").value("OPEN"))
                .andReturn();
        long commentId = objectMapper.readTree(changeResult.getResponse().getContentAsString())
                .path("comment").path("id").asLong();

        mockMvc.perform(as(anna, post("/api/translations/%d/submit".formatted(translationId))))
                .andExpect(status().isConflict());

        mockMvc.perform(as(anna, post("/api/editorial-comments/%d/resolve".formatted(commentId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(as(anna, patch("/api/translations/%d".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Save changes\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Save changes"))
                .andExpect(jsonPath("$.status").value(TranslationStatus.REJECTED.name()));

        submit(anna, translationId);
        mockMvc.perform(as(boris, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TranslationStatus.APPROVED.name()));

        mockMvc.perform(as(anna, post("/api/translation-projects/%d/complete".formatted(projectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ProjectStatus.COMPLETED.name()));

        mockMvc.perform(as(anna, post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Ещё","sequenceNumber":2}
                                """.formatted(projectId))))
                .andExpect(status().isConflict());

        mockMvc.perform(as(admin, delete("/api/users/%d".formatted(anna.id()))))
                .andExpect(status().isConflict());

        mockMvc.perform(as(anna, delete("/api/translation-projects/%d".formatted(projectId))))
                .andExpect(status().isNoContent());
        mockMvc.perform(as(anna, get("/api/translation-projects/%d".formatted(projectId))))
                .andExpect(status().isNotFound());
    }
}
