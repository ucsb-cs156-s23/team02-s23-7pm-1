package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Phone;
import edu.ucsb.cs156.example.repositories.PhoneRepository;

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

@WebMvcTest(controllers = PhonesController.class)
@Import(TestConfig.class)
public class PhonesControllerTests extends ControllerTestCase {

        @MockBean
        PhoneRepository phoneRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/phones/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/phones/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/phones/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/phones?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/phones/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/phones/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/phones/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Phone phone = Phone.builder()
                                .brand("Apple")
                                .model("iPhone 14")
                                .price(799)
                                .build();

                when(phoneRepository.findById(eq(7L))).thenReturn(Optional.of(phone));  // Check not sure why id is 7

                // act
                MvcResult response = mockMvc.perform(get("/api/phones?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(phoneRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(phone);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(phoneRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/phones?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(phoneRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Phone with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_phones() throws Exception {

                // arrange

                Phone phone1 = Phone.builder()
                                .brand("Apple")
                                .model("iPhone 14")
                                .price(799)
                                .build();

                Phone phone2 = Phone.builder()
                                .brand("Samsung")
                                .model("Galaxy S22")
                                .price(699)
                                .build();

                ArrayList<Phone> expectedPhones = new ArrayList<>();
                expectedPhones.addAll(Arrays.asList(phone1, phone2));

                when(phoneRepository.findAll()).thenReturn(expectedPhones);

                // act
                MvcResult response = mockMvc.perform(get("/api/phones/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(phoneRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedPhones);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_phone() throws Exception {
                // arrange

                Phone phone1 = Phone.builder()
                                .brand("Apple")
                                .model("iPhoneX")
                                .price(999)
                                .build();

                when(phoneRepository.save(eq(phone1))).thenReturn(phone1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/phones/post?brand=Apple&model=iPhoneX&price=999")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(phoneRepository, times(1)).save(phone1);
                String expectedJson = mapper.writeValueAsString(phone1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Phone phone1 = Phone.builder()
                                .brand("Google")
                                .model("Pixel 6")
                                .price(599)
                                .build();

                when(phoneRepository.findById(eq(15L))).thenReturn(Optional.of(phone1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/phones?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(phoneRepository, times(1)).findById(15L);
                verify(phoneRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Phone with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_phone_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(phoneRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/phones?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(phoneRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Phone with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_phone() throws Exception {
                // arrange

                Phone phoneOrig = Phone.builder()
                                .brand("LG")
                                .model("Q8")
                                .price(699)
                                .build();

                Phone phoneEdited = Phone.builder()
                                .brand("Motorola")
                                .model("G5S")
                                .price(599)
                                .build();

                String requestBody = mapper.writeValueAsString(phoneEdited);

                when(phoneRepository.findById(eq(67L))).thenReturn(Optional.of(phoneOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/phones?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(phoneRepository, times(1)).findById(67L);
                verify(phoneRepository, times(1)).save(phoneEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_phone_that_does_not_exist() throws Exception {
                // arrange

                Phone editedPhone = Phone.builder()
                                .brand("Samsung")
                                .model("Galaxy Note 10")
                                .price(1099)
                                .build();


                String requestBody = mapper.writeValueAsString(editedPhone);

                when(phoneRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/phones?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(phoneRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Phone with id 67 not found", json.get("message"));

        }
}
