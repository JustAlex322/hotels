package ru.alex.hotels.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.alex.hotels.services.HotelService;
import ru.alex.hotels.tdo.Hotel;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alex.hotels.dataForTests.HotelDataTest.*;

@WebMvcTest(HotelController.class)
class HotelControllerTest {
    private final String url = "/hotels";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HotelService hotelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testResponseCreateHotel() throws Exception {

        when(hotelService.createHotel(any(Hotel.class), any(String.class), any(String.class))).thenReturn(testHotelForCreate());

        mockMvc.perform(post(url + "?cityName=Белгород&directorFcs=Саша?")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testHotel())))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("У Саши"));
    }



    @Test
    void testGetAllHotelsStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/all"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllHotelsResponse() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(testListHotels());

        MvcResult response =  mockMvc.perform(MockMvcRequestBuilders.get(url + "/all")).andReturn();

        Hotel[] hotels = objectMapper.readValue(response.getResponse().getContentAsString(), Hotel[].class);

        Assertions.assertEquals(2, hotels.length);
    }
    @Test
    void testGetHotelByIdResponse() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(testHotelForCreate());

        mockMvc.perform(MockMvcRequestBuilders.get(url + "?id=1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testHotelForCreate().getName()));
    }

    @Test
    void testUpdateHotel() throws Exception {
        when(hotelService.updateHotel(testHotel(), 1L)).thenReturn(testHotelForCreate());

        mockMvc.perform(put(url + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testHotel())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(testHotel().getName()));
    }
}