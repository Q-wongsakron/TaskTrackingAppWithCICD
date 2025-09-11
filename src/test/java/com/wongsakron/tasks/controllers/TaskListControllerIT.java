package com.wongsakron.tasks.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskListControllerIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    static record TLReq(String title, String description){
    }
    static record TLUpdate(java.util.UUID id, String title, String description){
    }

    private String createListAndReturnId(String title, String desc) throws Exception {
        var res = mvc.perform(post("/task-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new TLReq(title, desc))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();

        JsonNode body = om.readTree(res.getResponse().getContentAsString());
        return body.get("id").asText();
    }

    @Test
    void create_shouldReturn200_andJsonBody() throws Exception {
        mvc.perform(post("/task-lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(new TLReq("My List", "D"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("My List"));
    }

    @Test
    void create_shouldReturn400_whenServiceValidatesAndThrows() throws Exception {
        // case title = null -> service should throw IllegalArgumentException
        mvc.perform(post("/task-lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(new TLReq(null, "d"))))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("Title"))); // message from service
    }

    // test Get /task-lists (list)
    @Test
    void list_shouldReturn200_andArray() throws Exception {
        createListAndReturnId("A", "a"); // create 1 list

        mvc.perform(get("/task-lists"))
                .andExpect(status().isOk()) // assume return 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // is json
                .andExpect(jsonPath("$").isArray()) // response should array
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1))); // array should have less 1 element
    }

    // test get /task-lists/{id}
    @Test
    void get_shouldReturn200_withBody_whenExists_and200_emptyWhenNotFound() throws Exception{
        var id = createListAndReturnId("B", "b");

        // case:found
        mvc.perform(get("/task-lists/{task_list_id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id)) // check id match
                .andExpect(jsonPath("$.title").value("B")); // chack title

        // case:not found -> controller return Option.empty() -> body = ""
        mvc.perform(get("/task-lists/{task_list_id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(content().string("null")); // body empty
    }
    // test: PUT /task-lists/{id}
    @Test
    void update_shouldReturn200_andPersistChanges() throws Exception {
        var idStr =createListAndReturnId("C", "c");
        var id = UUID.fromString(idStr);

        // body.id equal path id for pass validation in service
        var okBody = new TLUpdate(id, "C2", "c2");

        mvc.perform(put("/task-lists/{task_list_id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(okBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("C2"))
                .andExpect(jsonPath("$.description").value("c2"));
    }

    @Test
    void update_shouldReturn404_whenNotFound_butValidationPasses() throws Exception {
        // new UUID
        var missingId = UUID.randomUUID();

        //   for pass validation
        // - body.id != null
        // - body.id == path id
        var body = new TLUpdate(missingId, "X", "x");

        mvc.perform(put("/task-lists/{task_list_id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_shouldReturn400_whenBodyIdMissing_orIdMismatch() throws Exception {
        var idStr = createListAndReturnId("D", "d");
        var id = UUID.fromString(idStr);

        // case 1: no send id ใน body -> service throw IllegalArgumentException -> 400
        var missingIdJson = new TLReq("XD", "xd");
        mvc.perform(put("/task-lists/{task_list_id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(missingIdJson)))
                .andExpect(status().isBadRequest());

        // case 2: body.id != path id -> 400
        var otherId = UUID.randomUUID();
        var mismatchBody = new TLUpdate(otherId, "DX", "dx");
        mvc.perform(put("/task-lists/{task_list_id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(mismatchBody)))
                .andExpect(status().isBadRequest());
    }

    // test: DELETE /task-lists/{id}
    @Test
    void delete_shouldReturn200_andGetAfterDeleteShouldBeEmptyBody() throws Exception{
        var id = createListAndReturnId("D", "d");

        mvc.perform(delete("/task-lists/{task_list_id}", id))
                .andExpect(status().isOk());

        // check get after delete -> body is null
        mvc.perform(get("/task-lists/{task_list_id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }


}
