package org.example;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- helpers ----------

    private long post(String url, String json) throws Exception {
        String body = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createUser() throws Exception {
        String name = "u_" + UUID.randomUUID().toString().substring(0, 8);
        return post("/api/users", """
                {"username":"%s","email":"%s@example.com","password":"secret1"}""".formatted(name, name));
    }

    private long createProject(long ownerId) throws Exception {
        return post("/api/translation-projects", """
                {"name":"P","targetLanguage":"German","ownerId":%d}""".formatted(ownerId));
    }

    private long createSegment(long projectId, int order) throws Exception {
        return post("/api/source-segments", """
                {"projectId":%d,"sourceText":"text %d","order":%d}""".formatted(projectId, order, order));
    }

    private long createTranslation(long segmentId, long authorId) throws Exception {
        return post("/api/translations", """
                {"segmentId":%d,"authorId":%d,"text":"Hallo"}""".formatted(segmentId, authorId));
    }

    private void patch(String url, String json, int expectedStatus) throws Exception {
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(expectedStatus));
    }

    // ---------- list endpoints ----------

    @Test
    void listEndpointsReturnSeedData() throws Exception {
        for (String url : new String[]{"/api/translation-projects", "/api/source-segments",
                "/api/translations", "/api/editorial-comments", "/api/users"}) {
            mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$").isNotEmpty());
        }
    }

    // ---------- users ----------

    @Test
    void passwordIsNeverReturned() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void duplicateUsernameIsRejectedOnCreateAndUpdate() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john_doe","email":"x@example.com","password":"secret1"}"""))
                .andExpect(status().isConflict());

        long id = createUser();
        patch("/api/users/" + id, "{\"username\":\"john_doe\"}", 409);
    }

    @Test
    void invalidUserIsRejected() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- error handling ----------

    @Test
    void unknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/users/999999")).andExpect(status().isNotFound());
        patch("/api/users/999999", "{}", 404);
        mockMvc.perform(delete("/api/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void badInputReturns400Not500() throws Exception {
        mockMvc.perform(get("/api/users/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
    }

    // ---------- PATCH semantics ----------

    @Test
    void patchChangesOnlySentFields() throws Exception {
        long author = createUser();
        long project = createProject(author);
        long segment = createSegment(project, 1);
        long translation = createTranslation(segment, author);
        long comment = post("/api/editorial-comments", """
                {"translationId":%d,"commentText":"Please check"}""".formatted(translation));

        patch("/api/editorial-comments/" + comment, "{\"status\":\"RESOLVED\"}", 200);

        mockMvc.perform(get("/api/editorial-comments/" + comment))
                .andExpect(jsonPath("$.commentText").value("Please check"))
                .andExpect(jsonPath("$.translationId").value(translation))
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void clientCannotSetId() throws Exception {
        long author = createUser();
        // id из тела игнорируется, существующая запись (id=1) не перезаписывается
        String body = mockMvc.perform(post("/api/translation-projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"X\",\"targetLanguage\":\"Italian\",\"ownerId\":" + author + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long newId = ((Number) JsonPath.read(body, "$.id")).longValue();
        org.junit.jupiter.api.Assertions.assertNotEquals(1L, newId);
        mockMvc.perform(get("/api/translation-projects/1")).andExpect(jsonPath("$.name").value("Project Alpha"));
    }

    // ---------- references and cascade ----------

    @Test
    void referencesMustExist() throws Exception {
        mockMvc.perform(post("/api/source-segments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":999999,\"sourceText\":\"x\",\"order\":1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/translation-projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"P\",\"targetLanguage\":\"German\",\"ownerId\":999999}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingProjectCascades() throws Exception {
        long author = createUser();
        long project = createProject(author);
        long segment = createSegment(project, 1);
        long translation = createTranslation(segment, author);
        long comment = post("/api/editorial-comments", """
                {"translationId":%d,"commentText":"c"}""".formatted(translation));

        mockMvc.perform(delete("/api/translation-projects/" + project)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/source-segments/" + segment)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/translations/" + translation)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/editorial-comments/" + comment)).andExpect(status().isNotFound());
    }

    @Test
    void deletingUserCascadesToOwnedProjectsAndAuthoredTranslations() throws Exception {
        long owner = createUser();
        long author = createUser();
        long project = createProject(owner);
        long segment = createSegment(project, 1);
        long translation = createTranslation(segment, author);   // автор - другой пользователь

        // удаляем автора: его перевод исчезает, чужой проект и сегмент остаются
        mockMvc.perform(delete("/api/users/" + author)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/translations/" + translation)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/source-segments/" + segment)).andExpect(status().isOk());

        // удаляем владельца: проект и сегмент исчезают
        mockMvc.perform(delete("/api/users/" + owner)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/translation-projects/" + project)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/source-segments/" + segment)).andExpect(status().isNotFound());
    }

    // ---------- business rules ----------

    @Test
    void segmentsOfProjectAreSortedByOrder() throws Exception {
        long owner = createUser();
        long project = createProject(owner);
        createSegment(project, 3);
        createSegment(project, 1);
        createSegment(project, 2);

        mockMvc.perform(get("/api/source-segments/project/" + project))
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[1].order").value(2))
                .andExpect(jsonPath("$[2].order").value(3));
    }

    @Test
    void translationDefaultsToDraftAndStatusCanBeChanged() throws Exception {
        long author = createUser();
        long segment = createSegment(createProject(author), 1);
        long translation = createTranslation(segment, author);
        String url = "/api/translations/" + translation;

        mockMvc.perform(get(url)).andExpect(jsonPath("$.status").value("DRAFT"));
        patch(url, "{\"status\":\"APPROVED\"}", 200);
        mockMvc.perform(get(url)).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void clientCannotSetServerTime() throws Exception {
        long author = createUser();
        String body = mockMvc.perform(post("/api/translation-projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"T\",\"targetLanguage\":\"German\",\"ownerId\":" + author
                                + ",\"createdAt\":\"2000-01-01T00:00:00\",\"updatedAt\":\"2000-01-01T00:00:00\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String createdAt = JsonPath.read(body, "$.createdAt");
        org.junit.jupiter.api.Assertions.assertFalse(createdAt.startsWith("2000"));
    }
}
