package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Негативные сценарии и правила целостности. Каждый тест создаёт свои данные
 * с уникальными именами, чтобы не зависеть от других тестов в общей in-memory БД.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- ошибки HTTP-слоя ----------

    @Test
    void malformedIdIsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownUrlIsNotFound() throws Exception {
        mockMvc.perform(get("/api/no-such-resource"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethodIsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/api/users/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unsupportedContentTypeIs415() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("anything"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ---------- валидация ----------

    @Test
    void tooLongUsernameIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@x.local","role":"TRANSLATOR"}
                                """.formatted("u".repeat(65), unique("long"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankTargetLanguageInPatchIsBadRequest() throws Exception {
        long ownerId = createUser("owner");
        long projectId = createProject(ownerId);

        mockMvc.perform(patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetLanguage\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailIsCaseInsensitive() throws Exception {
        String local = unique("mixed");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@Studio.Local","role":"EDITOR"}
                                """.formatted(unique("first"), local)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(local + "@studio.local"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@STUDIO.LOCAL","role":"EDITOR"}
                                """.formatted(unique("second"), local)))
                .andExpect(status().isConflict());
    }

    // ---------- защита утверждённого перевода ----------

    @Test
    void approvedTranslationLocksSegment() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(patch("/api/source-segments/%d".formatted(flow.segmentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Другой текст\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/source-segments/%d".formatted(flow.segmentId)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/translations/%d".formatted(flow.translationId)))
                .andExpect(status().isConflict());
    }

    @Test
    void secondTranslationForApprovedSegmentIsRejected() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"Another"}
                                """.formatted(flow.segmentId, flow.authorId)))
                .andExpect(status().isConflict());
    }

    @Test
    void sequenceNumberCanChangeWhileTranslationIsApproved() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(patch("/api/source-segments/%d".formatted(flow.segmentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequenceNumber\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequenceNumber").value(5));
    }

    @Test
    void unapprovedTranslationOfCompletedProjectCannotBeDeleted() throws Exception {
        Flow flow = draftFlow();
        long spareTranslationId = id(createTranslationRaw(flow.segmentId, flow.authorId, "Spare"));

        mockMvc.perform(post("/api/translations/%d/submit".formatted(flow.translationId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/translations/%d/approve".formatted(flow.translationId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/translation-projects/%d/complete".formatted(flow.projectId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/translations/%d".formatted(spareTranslationId)))
                .andExpect(status().isConflict());
    }

    // ---------- единый путь замечания ----------

    @Test
    void directCommentReturnsTranslationForRework() throws Exception {
        Flow flow = submittedFlow();

        mockMvc.perform(post("/api/editorial-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"translationId":%d,"content":"Поправьте"}
                                """.formatted(flow.translationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/translations/%d".formatted(flow.translationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(post("/api/translations/%d/approve".formatted(flow.translationId)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/translations/%d/submit".formatted(flow.translationId)))
                .andExpect(status().isConflict());
    }

    @Test
    void commentOnDraftTranslationIsConflict() throws Exception {
        Flow flow = draftFlow();

        mockMvc.perform(post("/api/editorial-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"translationId":%d,"content":"Рано"}
                                """.formatted(flow.translationId)))
                .andExpect(status().isConflict());
    }

    // ---------- сценарные помощники ----------

    private record Flow(long authorId, long projectId, long segmentId, long translationId) {
    }

    private Flow draftFlow() throws Exception {
        long authorId = createUser("author");
        long projectId = createProject(authorId);
        long segmentId = createSegment(projectId, 1);
        mockMvc.perform(post("/api/translation-projects/%d/start".formatted(projectId)))
                .andExpect(status().isOk());
        MvcResult result = createTranslationRaw(segmentId, authorId, "Text");
        long translationId = id(result);
        return new Flow(authorId, projectId, segmentId, translationId);
    }

    private Flow submittedFlow() throws Exception {
        Flow flow = draftFlow();
        mockMvc.perform(post("/api/translations/%d/submit".formatted(flow.translationId)))
                .andExpect(status().isOk());
        return flow;
    }

    private Flow approvedFlow() throws Exception {
        Flow flow = submittedFlow();
        mockMvc.perform(post("/api/translations/%d/approve".formatted(flow.translationId)))
                .andExpect(status().isOk());
        return flow;
    }

    private MvcResult createTranslationRaw(long segmentId, long authorId, String text) throws Exception {
        ResultActions actions = mockMvc.perform(post("/api/translations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"segmentId":%d,"authorId":%d,"text":"%s"}
                        """.formatted(segmentId, authorId, text)));
        return actions.andReturn();
    }

    private long createUser(String prefix) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","role":"TRANSLATOR"}
                                """.formatted(unique(prefix), unique(prefix))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private long createProject(long ownerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private long createSegment(long projectId, int sequenceNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Сохранить","sequenceNumber":%d}
                                """.formatted(projectId, sequenceNumber)))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private long id(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("id").asLong();
    }
}
