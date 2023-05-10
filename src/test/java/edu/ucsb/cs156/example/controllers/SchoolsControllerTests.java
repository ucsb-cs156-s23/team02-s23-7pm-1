package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Schools;
import edu.ucsb.cs156.example.repositories.SchoolsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = SchoolsController.class)
@Import(TestConfig.class)
public class SchoolsControllerTests extends ControllerTestCase {

        @MockBean
        SchoolsRepository schoolsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/schools/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/schools/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/schools/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/schools/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Schools school = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6")
                                .build();

                when(schoolsRepository.findById(eq(7L))).thenReturn(Optional.of(school));

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(schoolsRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(school);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(schoolsRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(schoolsRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Schools with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_schools() throws Exception {

                // arrange

                Schools school1 = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6")      
                                .build();

                Schools school2 = Schools.builder()
                                .name("Carpinteria High School")
                                .district("Carpinteria Unified School District")
                                .gradeRange("9-12") 
                                .build();

                ArrayList<Schools> expectedSchools = new ArrayList<>();
                expectedSchools.addAll(Arrays.asList(school1, school2));

                when(schoolsRepository.findAll()).thenReturn(expectedSchools);

                // act
                MvcResult response = mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(schoolsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedSchools);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_school() throws Exception {
                // arrange

                Schools school1 = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6")  
                                .build();

                when(schoolsRepository.save(eq(school1))).thenReturn(school1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/schools/post?name=Isla Vista Elementary School&district=Goleta Union School District&gradeRange=K-6")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolsRepository, times(1)).save(school1);
                String expectedJson = mapper.writeValueAsString(school1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_school() throws Exception {
                // arrange

                Schools school1 = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6")  
                                .build();

                when(schoolsRepository.findById(eq(15L))).thenReturn(Optional.of(school1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolsRepository, times(1)).findById(15L);
                verify(schoolsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("School with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_school_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(schoolsRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(schoolsRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Schools with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_school() throws Exception {
                // arrange

                Schools schoolOrig = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6") 
                                .build();

                Schools schoolEdited = Schools.builder()
                                .name("Goleta Valley Junior High School")
                                .district("Santa Barbara Unified School District")
                                .gradeRange("7-8") 
                                .build();

                String requestBody = mapper.writeValueAsString(schoolEdited);

                when(schoolsRepository.findById(eq(67L))).thenReturn(Optional.of(schoolOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolsRepository, times(1)).findById(67L);
                verify(schoolsRepository, times(1)).save(schoolEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_school_that_does_not_exist() throws Exception {
                // arrange

                Schools schoolEdited = Schools.builder()
                                .name("Isla Vista Elementary School")
                                .district("Goleta Union School District")
                                .gradeRange("K-6") 
                                .build();

                String requestBody = mapper.writeValueAsString(schoolEdited);

                when(schoolsRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(schoolsRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Schools with id 67 not found", json.get("message"));

        }
}
