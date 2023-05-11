package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Major;
import edu.ucsb.cs156.example.repositories.MajorRepository;

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

@WebMvcTest(controllers = MajorsController.class)
@Import(TestConfig.class)
public class MajorsControllerTests extends ControllerTestCase {

        @MockBean
        MajorRepository majorRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/majors/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/majors/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/majors/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/majors?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/majors/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/majors/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/majors/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                Major major = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                when(majorRepository.findById(eq(7L))).thenReturn(Optional.of(major));

                // act
                MvcResult response = mockMvc.perform(get("/api/majors?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(majorRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(major);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(majorRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/majors?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(majorRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Major with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_majors() throws Exception {

                // arrange
                Major major1 = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                Major major2 = Major.builder()
                                .name("Biology")
                                .department("LS")
                                .degreePursued("B.S.")
                                .build();

                ArrayList<Major> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(major1, major2));

                when(majorRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/majors/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(majorRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_major() throws Exception {
                // arrange

                Major major1 = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                when(majorRepository.save(eq(major1))).thenReturn(major1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/majors/post?name=Computer Engineering&department=COE&degreePursued=B.S.")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(majorRepository, times(1)).save(major1);
                String expectedJson = mapper.writeValueAsString(major1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_major() throws Exception {
                // arrange

                Major major1 = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                when(majorRepository.findById(eq(15L))).thenReturn(Optional.of(major1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/majors?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(majorRepository, times(1)).findById(15L);
                verify(majorRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Major with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_major_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(majorRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/majors?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(majorRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Major with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_major() throws Exception {
                // arrange

                Major majorOrig = Major.builder()
                                .name("Data Science")
                                .department("LS")
                                .degreePursued("B.S.")
                                .build();

                Major majorEdited = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                String requestBody = mapper.writeValueAsString(majorEdited);

                when(majorRepository.findById(eq(67L))).thenReturn(Optional.of(majorOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/majors?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(majorRepository, times(1)).findById(67L);
                verify(majorRepository, times(1)).save(majorEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_major_that_does_not_exist() throws Exception {
                // arrange

                Major editedMajor = Major.builder()
                                .name("Computer Engineering")
                                .department("COE")
                                .degreePursued("B.S.")
                                .build();

                String requestBody = mapper.writeValueAsString(editedMajor);

                when(majorRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/majors?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(majorRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Major with id 67 not found", json.get("message"));
        }
}
