package org.example;

import org.example.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RulesTest extends ApiTestBase {

    private record Flow(Account author, Account editor, long projectId, long segmentId, long translationId) {
    }

    @Test
    void malformedIdIsBadRequest() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, get("/api/users/abc")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownUrlIsNotFound() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, get("/api/no-such-resource")))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethodIsMethodNotAllowed() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, put("/api/users/1")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unsupportedContentTypeIs415() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, post("/api/users")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("anything")))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void tooLongUsernameIsBadRequest() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@x.local","password":"%s","role":"TRANSLATOR"}
                                """.formatted("u".repeat(65), unique("long"), PASSWORD))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankTargetLanguageInPatchIsBadRequest() throws Exception {
        Account owner = account(UserRole.TRANSLATOR);
        long projectId = createProject(owner, owner.id());

        mockMvc.perform(as(owner, patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetLanguage\":\"  \"}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailIsCaseInsensitive() throws Exception {
        Account admin = account(UserRole.ADMIN);
        String local = unique("mixed");
        mockMvc.perform(as(admin, post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@Studio.Local","password":"%s","role":"EDITOR"}
                                """.formatted(unique("first"), local, PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(local + "@studio.local"));

        mockMvc.perform(as(admin, post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@STUDIO.LOCAL","password":"%s","role":"EDITOR"}
                                """.formatted(unique("second"), local, PASSWORD))))
                .andExpect(status().isConflict());
    }

    @Test
    void approvedTranslationLocksSegment() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(as(flow.author(), patch("/api/source-segments/%d".formatted(flow.segmentId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Другой текст\"}")))
                .andExpect(status().isConflict());

        mockMvc.perform(as(flow.author(), delete("/api/source-segments/%d".formatted(flow.segmentId()))))
                .andExpect(status().isConflict());

        mockMvc.perform(as(flow.author(), delete("/api/translations/%d".formatted(flow.translationId()))))
                .andExpect(status().isConflict());
    }

    @Test
    void secondTranslationForApprovedSegmentIsRejected() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(as(flow.author(), post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"Another"}
                                """.formatted(flow.segmentId(), flow.author().id()))))
                .andExpect(status().isConflict());
    }

    @Test
    void sequenceNumberCanChangeWhileTranslationIsApproved() throws Exception {
        Flow flow = approvedFlow();

        mockMvc.perform(as(flow.author(), patch("/api/source-segments/%d".formatted(flow.segmentId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequenceNumber\":5}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequenceNumber").value(5));
    }

    @Test
    void unapprovedTranslationOfCompletedProjectCannotBeDeleted() throws Exception {
        Flow flow = draftFlow();
        long spareTranslationId = createTranslation(flow.author(), flow.segmentId(), flow.author().id(), "Spare");

        submit(flow.author(), flow.translationId());
        approve(flow.editor(), flow.translationId());
        mockMvc.perform(as(flow.author(), post("/api/translation-projects/%d/complete".formatted(flow.projectId()))))
                .andExpect(status().isOk());

        mockMvc.perform(as(flow.author(), delete("/api/translations/%d".formatted(spareTranslationId))))
                .andExpect(status().isConflict());
    }

    @Test
    void directCommentReturnsTranslationForRework() throws Exception {
        Flow flow = submittedFlow();

        mockMvc.perform(as(flow.editor(), post("/api/editorial-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"translationId":%d,"content":"Поправьте"}
                                """.formatted(flow.translationId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(as(flow.author(), get("/api/translations/%d".formatted(flow.translationId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(as(flow.editor(), post("/api/translations/%d/approve".formatted(flow.translationId()))))
                .andExpect(status().isConflict());
        mockMvc.perform(as(flow.author(), post("/api/translations/%d/submit".formatted(flow.translationId()))))
                .andExpect(status().isConflict());
    }

    @Test
    void commentOnDraftTranslationIsConflict() throws Exception {
        Flow flow = draftFlow();

        mockMvc.perform(as(flow.editor(), post("/api/editorial-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"translationId":%d,"content":"Рано"}
                                """.formatted(flow.translationId()))))
                .andExpect(status().isConflict());
    }

    private Flow draftFlow() throws Exception {
        Account author = account(UserRole.TRANSLATOR);
        Account editor = account(UserRole.EDITOR);
        long projectId = createProject(author, author.id());
        long segmentId = createSegment(author, projectId, 1);
        startProject(author, projectId);
        long translationId = createTranslation(author, segmentId, author.id(), "Text");
        return new Flow(author, editor, projectId, segmentId, translationId);
    }

    private Flow submittedFlow() throws Exception {
        Flow flow = draftFlow();
        submit(flow.author(), flow.translationId());
        return flow;
    }

    private Flow approvedFlow() throws Exception {
        Flow flow = submittedFlow();
        approve(flow.editor(), flow.translationId());
        return flow;
    }
}
