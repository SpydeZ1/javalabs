package org.example;

import org.example.model.UserRole;
import org.example.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityTest extends ApiTestBase {

    private static final String BAD_CREDENTIALS = "Неверное имя пользователя или пароль";

    @Test
    void registrationCreatesTranslatorAndIgnoresRequestedRole() throws Exception {
        String username = unique("newbie");
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s","role":"ADMIN","status":"BLOCKED"}
                                """.formatted(username, username, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("TRANSLATOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/users/me").with(httpBasic(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void registrationRequiresCsrfToken() throws Exception {
        String username = unique("nocsrf");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s"}
                                """.formatted(username, username, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Отсутствует или недействителен CSRF-токен"));
    }

    @Test
    void weakPasswordsAreRejected() throws Exception {
        for (String weak : new String[]{"Sh1A#", "alllowercase1#", "ALLUPPERCASE1#", "NoDigitsHere#", "NoSpecial1234", "Has Space1#a"}) {
            String username = unique("weak");
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"%s","email":"%s@studio.local","password":"%s"}
                                    """.formatted(username, username, weak)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void passwordMustNotContainUsername() throws Exception {
        String username = unique("selfpass");
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"A1#%s"}
                                """.formatted(username, username, username)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateUsernameIsConflict() throws Exception {
        Account existing = account(UserRole.TRANSLATOR);
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s"}
                                """.formatted(existing.username(), unique("other"), PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void unsafeRequestWithoutCsrfIsForbiddenEvenForAuthenticatedUser() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);
        mockMvc.perform(withoutCsrf(translator, post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(translator.id()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Отсутствует или недействителен CSRF-токен"));
    }

    @Test
    void anonymousRequestIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/translation-projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Требуется аутентификация"));
    }

    @Test
    void wrongPasswordAndUnknownUserLookTheSame() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);

        mockMvc.perform(get("/api/users/me").with(httpBasic(translator.username(), "Wrong#Pass123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(BAD_CREDENTIALS));

        mockMvc.perform(get("/api/users/me").with(httpBasic(unique("ghost"), PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(BAD_CREDENTIALS));
    }

    @Test
    void blockedUserCannotAuthenticate() throws Exception {
        Account blocked = account(UserRole.TRANSLATOR, UserStatus.BLOCKED);

        mockMvc.perform(as(blocked, get("/api/users/me")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(BAD_CREDENTIALS));
    }

    @Test
    void adminBlocksUserAndAccessIsLost() throws Exception {
        Account admin = account(UserRole.ADMIN);
        Account translator = account(UserRole.TRANSLATOR);

        mockMvc.perform(as(translator, get("/api/users/me")))
                .andExpect(status().isOk());

        mockMvc.perform(as(admin, patch("/api/users/%d".formatted(translator.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        mockMvc.perform(as(translator, get("/api/users/me")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(as(admin, post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(translator.id()))))
                .andExpect(status().isConflict());
    }

    @Test
    void userListNeverExposesPasswordData() throws Exception {
        Account admin = account(UserRole.ADMIN);
        mockMvc.perform(as(admin, get("/api/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    void translatorCannotManageUsers() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);
        Account other = account(UserRole.TRANSLATOR);

        mockMvc.perform(as(translator, get("/api/users")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(translator, post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@studio.local","password":"%s","role":"ADMIN"}
                                """.formatted(unique("evil"), unique("evil"), PASSWORD))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(translator, delete("/api/users/%d".formatted(other.id()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(translator, get("/api/users/%d".formatted(other.id()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(as(translator, get("/api/users/%d".formatted(translator.id()))))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotElevateOwnRoleButCanChangeEmail() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);

        mockMvc.perform(as(translator, patch("/api/users/%d".formatted(translator.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(translator, patch("/api/users/%d".formatted(translator.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\",\"email\":\"%s@studio.local\"}".formatted(unique("changed")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TRANSLATOR"));
    }

    @Test
    void adminCannotChangeOrDeleteOwnAccount() throws Exception {
        Account admin = account(UserRole.ADMIN);

        mockMvc.perform(as(admin, patch("/api/users/%d".formatted(admin.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EDITOR\"}")))
                .andExpect(status().isConflict());

        mockMvc.perform(as(admin, delete("/api/users/%d".formatted(admin.id()))))
                .andExpect(status().isConflict());
    }

    @Test
    void projectCanBeChangedOnlyByOwnerOrAdmin() throws Exception {
        Account owner = account(UserRole.TRANSLATOR);
        Account stranger = account(UserRole.TRANSLATOR);
        Account admin = account(UserRole.ADMIN);
        long projectId = createProject(owner, owner.id());

        mockMvc.perform(as(stranger, patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Чужой\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(stranger, delete("/api/translation-projects/%d".formatted(projectId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(stranger, post("/api/translation-projects/%d/start".formatted(projectId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(stranger, post("/api/source-segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"content":"Текст","sequenceNumber":1}
                                """.formatted(projectId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(owner, patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Свой\"}")))
                .andExpect(status().isOk());

        mockMvc.perform(as(owner, patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":%d}".formatted(stranger.id()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(admin, patch("/api/translation-projects/%d".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":%d}".formatted(stranger.id()))))
                .andExpect(status().isOk());
    }

    @Test
    void projectCannotBeCreatedForSomeoneElseByRegularUser() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);
        Account other = account(UserRole.TRANSLATOR);

        mockMvc.perform(as(translator, post("/api/translation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Проект","targetLanguage":"en","ownerId":%d}
                                """.formatted(other.id()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingResourceIsNotFoundEvenForRegularUser() throws Exception {
        Account translator = account(UserRole.TRANSLATOR);

        mockMvc.perform(as(translator, delete("/api/translation-projects/999999")))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(translator, delete("/api/translations/999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void reviewOperationsAreLimitedToEditorsAndNeverToTheAuthor() throws Exception {
        Account author = account(UserRole.TRANSLATOR);
        Account editor = account(UserRole.EDITOR);
        Account otherTranslator = account(UserRole.TRANSLATOR);
        long projectId = createProject(author, author.id());
        long segmentId = createSegment(author, projectId, 1);
        startProject(author, projectId);
        long translationId = createTranslation(author, segmentId, author.id(), "Text");
        submit(author, translationId);

        mockMvc.perform(as(author, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(otherTranslator, post("/api/translations/%d/request-changes".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Нет\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(otherTranslator, post("/api/editorial-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"translationId":%d,"content":"Нет"}
                                """.formatted(translationId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(otherTranslator, patch("/api/translations/%d".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Чужой текст\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(editor, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isOk());
    }

    @Test
    void adminAsAuthorCannotApproveOwnTranslation() throws Exception {
        Account admin = account(UserRole.ADMIN);
        Account editor = account(UserRole.EDITOR);
        long projectId = createProject(admin, admin.id());
        long segmentId = createSegment(admin, projectId, 1);
        startProject(admin, projectId);
        long translationId = createTranslation(admin, segmentId, admin.id(), "Text");
        submit(admin, translationId);

        mockMvc.perform(as(admin, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(editor, post("/api/translations/%d/approve".formatted(translationId))))
                .andExpect(status().isOk());
    }

    @Test
    void editorCannotAuthorTranslationsAndTranslatorCannotSpoofAuthor() throws Exception {
        Account author = account(UserRole.TRANSLATOR);
        Account editor = account(UserRole.EDITOR);
        Account other = account(UserRole.TRANSLATOR);
        long projectId = createProject(author, author.id());
        long segmentId = createSegment(author, projectId, 1);
        startProject(author, projectId);

        mockMvc.perform(as(editor, post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"Text"}
                                """.formatted(segmentId, editor.id()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(other, post("/api/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"segmentId":%d,"authorId":%d,"text":"Text"}
                                """.formatted(segmentId, author.id()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void commentsCanBeResolvedByTranslationAuthorAndEditorsOnly() throws Exception {
        Account author = account(UserRole.TRANSLATOR);
        Account editor = account(UserRole.EDITOR);
        Account stranger = account(UserRole.TRANSLATOR);
        long projectId = createProject(author, author.id());
        long segmentId = createSegment(author, projectId, 1);
        startProject(author, projectId);
        long translationId = createTranslation(author, segmentId, author.id(), "Text");
        submit(author, translationId);

        MvcResult change = mockMvc.perform(as(editor, post("/api/translations/%d/request-changes".formatted(translationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Переделать\"}")))
                .andExpect(status().isOk())
                .andReturn();
        long commentId = objectMapper.readTree(change.getResponse().getContentAsString())
                .path("comment").path("id").asLong();

        mockMvc.perform(as(stranger, post("/api/editorial-comments/%d/resolve".formatted(commentId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(author, patch("/api/editorial-comments/%d".formatted(commentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Свой комментарий\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(author, post("/api/editorial-comments/%d/resolve".formatted(commentId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
