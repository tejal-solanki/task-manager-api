package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAllTasks_shouldReturn200() throws Exception {
        mockMvc.perform(get("/task"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTasks_withoutAuth_shouldReturn403() throws Exception {
        mockMvc.perform(get("/task"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createTask_shouldReturn201() throws Exception {
        TaskManager task = new TaskManager("Test", "Desc", TaskStatus.TODO, LocalDate.now(),LocalDate.now().plusDays(3));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());
    }
}