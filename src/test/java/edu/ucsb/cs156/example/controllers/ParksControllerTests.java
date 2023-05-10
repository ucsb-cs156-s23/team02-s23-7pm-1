package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Park;
import edu.ucsb.cs156.example.repositories.ParkRepository;

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

@WebMvcTest(controllers = ParksController.class)
@Import(TestConfig.class)
public class ParksControllerTests extends ControllerTestCase {

        @MockBean
        ParkRepository parkRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/parks/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/parks/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/parks/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/parks?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/parks/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/parks/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/parks/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                Park park = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                when(parkRepository.findById(eq(7L))).thenReturn(Optional.of(park));

                // act
                MvcResult response = mockMvc.perform(get("/api/parks?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(parkRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(park);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(parkRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/parks?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(parkRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Park with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_parks() throws Exception {

                // arrange
                Park park1 = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                Park park2 = Park.builder()
                                .name("Grand Canyon National Park")
                                .state("Arizona")
                                .acres(1218375)
                                .build();

                ArrayList<Park> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(park1, park2));

                when(parkRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/parks/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(parkRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_park() throws Exception {
                // arrange

                Park park1 = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                when(parkRepository.save(eq(park1))).thenReturn(park1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/parks/post?name=Yosemite National Park&state=California&acres=761747")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(parkRepository, times(1)).save(park1);
                String expectedJson = mapper.writeValueAsString(park1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_park() throws Exception {
                // arrange

                Park park1 = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                when(parkRepository.findById(eq(15L))).thenReturn(Optional.of(park1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/parks?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(parkRepository, times(1)).findById(15L);
                verify(parkRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Park with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_park_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(parkRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/parks?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(parkRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Park with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_park() throws Exception {
                // arrange

                Park parkOrig = Park.builder()
                                .name("Yosemite")
                                .state("CA")
                                .acres(0)
                                .build();

                Park parkEdited = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                String requestBody = mapper.writeValueAsString(parkEdited);

                when(parkRepository.findById(eq(67L))).thenReturn(Optional.of(parkOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/parks?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(parkRepository, times(1)).findById(67L);
                verify(parkRepository, times(1)).save(parkEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_park_that_does_not_exist() throws Exception {
                // arrange

                Park editedPark = Park.builder()
                                .name("Yosemite National Park")
                                .state("California")
                                .acres(761747)
                                .build();

                String requestBody = mapper.writeValueAsString(editedPark);

                when(parkRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/parks?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(parkRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Park with id 67 not found", json.get("message"));

        }
}
