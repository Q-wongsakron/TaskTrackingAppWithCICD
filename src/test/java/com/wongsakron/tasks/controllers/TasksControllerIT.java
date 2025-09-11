package com.wongsakron.tasks.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TasksControllerIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    // DTO record for serialize body
    static record TLReq(String title, String description){}
    static record TCreate(String title, String description, String dueDate, String priority, String status) {}
    static record TUpdate(String id, String title, String description, String dueDate, String priority, String status) {}

    // helper create tasklist and return id(string)
    private String createList() throws Exception {
        var res = mvc.perform(post("/task-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new TLReq("List", "D"))))
                .andExpect(status().isOk())
                .andReturn(); // contain result for read body

        JsonNode body = om.readTree(res.getResponse().getContentAsString()); // parse response JSON
        return body.get("id").asText(); // return id from list created

    }

    // helper create task in list and return taskId(string)
    private String createTask(String listId, String title) throws Exception {
        var due = LocalDateTime.now().plusDays(3).toString();
        var res = mvc.perform(post("/task-lists/{task_list_id}/tasks",listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(new TCreate(title, "D", due, null, null )))) // priority and status use default
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode body = om.readTree(res.getResponse().getContentAsString());
        return body.get("id").asText();
    }
    // create: happy path + 404 (not found list) + 400(title invalid)
    @Test
    void createTask_shouldReturn200_or404_whenListNotFound_or400_whenServiceValidates() throws Exception{
        var listId = createList();
        var due = LocalDateTime.now().plusDays(1).toString();

        // happy path: 200 and Json body
        mvc.perform(post("/task-lists/{task_list_id}/tasks", listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(new TCreate("T1", "D1", due, null, null))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").exists()) // must have id of task
                .andExpect(jsonPath("$.title").value("T1"));

        mvc.perform(post("/task-lists/{task_list_id}/tasks", UUID.randomUUID()) // wrong list id
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new TCreate("T2", "D2", due, null, null))))
                .andExpect(status().isNotFound()) // 404 from globalException
                .andExpect(jsonPath("$.status").value(404)); // 404 from body field status

        mvc.perform(post("/task-lists/{task_list_id}/tasks", listId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new TCreate(null, "D", due, null, null))))
                .andExpect(status().isBadRequest()) // 400 from globalException
                .andExpect(jsonPath("$.status").value(400)); // 400 from body field status

    }

    @Test
    void listTasks_shouldReturn200_andArray() throws Exception {
        var listId = createList();
        createTask(listId, "A");
        createTask(listId, "B");

        mvc.perform(get("/task-lists/{task_list_id}/tasks",listId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].title",hasItems("A","B")));
    }

    // get One: happy path and Notfound
    @Test
    void getTask_shouldReturn200_withBody_whenExists_and200_emptyWhenNotFound() throws Exception{
        var listId = createList();
        var taskId = createTask(listId, "T-get");


        // exists 200 + body json
        mvc.perform(get("/task-lists/{task_list_id}/tasks/{task_id}", listId, taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId)); // body must have id match created id

        // notfound 200 + body null
        mvc.perform(get("/task-lists/{task_list_id}/tasks/{task_id}", listId, UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(content().string("null"));

    }

    // UPDATE happy 404 not found + 400 id mismatch/invalid
    @Test
    void updateTask_shouldReturn200_or404_whenNotFound_or400_whenIdMismatchOrInvalid() throws Exception {
        var listId = createList();
        var taskId = createTask(listId, "T-old");
        var due = LocalDateTime.now().plusDays(5).toString();

        // happy path body send id match path param
        var body = new TUpdate(taskId, "T-new", "new desc", due, "HIGH", "CLOSED");

        mvc.perform(put("/task-lists/{task_list_id}/tasks/{task_id}", listId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("T-new"));

        var unknownTaskId = UUID.randomUUID();                             // no id in db
        var body404 = new TUpdate(unknownTaskId.toString(),                //  body.id == path id
                "Name", "desc", due, "HIGH", "OPEN");

        mvc.perform(put("/task-lists/{task_list_id}/tasks/{task_id}", listId, unknownTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body404)))
                .andExpect(status().isNotFound())                               // 404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));

        // id mismatch: body.id != path taskId -> service throw IllegalArgumentException -> 400
        var badBody = new TUpdate(UUID.randomUUID().toString(), "N", "D", due, "LOW", "OPEN"); // id in body not match
        mvc.perform(put("/task-lists/{task_list_id}/tasks/{task_id}", listId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(badBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // Delete: happy path 200 + check get after delete  = body empty
    @Test
    void deleteTask_shouldReturn200_andGetAfterDeleteShouldBeEmptyBody() throws Exception{
        var listId = createList();
        var taskId = createTask(listId, "T-del");

        mvc.perform(delete("/task-lists/{task_list_id}/tasks/{task_id}", listId, taskId))
                .andExpect(status().isOk());

        // after delete body is empty
        mvc.perform(get("/task-lists/{task_list_id}/tasks/{task_id}", listId, taskId))
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }



}
