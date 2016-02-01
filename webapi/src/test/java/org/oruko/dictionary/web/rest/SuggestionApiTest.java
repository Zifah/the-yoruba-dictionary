package org.oruko.dictionary.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.SuggestedName;
import org.oruko.dictionary.model.repository.SuggestedNameRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests {@link SuggestionApi}
 * @author Dadepo Aderemi.
 */
@RunWith(MockitoJUnitRunner.class)
public class SuggestionApiTest extends AbstractApiTest {

    @InjectMocks
    SuggestionApi suggestionApi;

    @Mock
    SuggestedNameRepository suggestedNameRepository;

    MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(suggestionApi).setHandlerExceptionResolvers(
                createExceptionResolver()).build();
    }

    @Test
    public void testGetSuggestedMetaData_empty() throws Exception {
        mockMvc.perform(get("/v1/suggestions/meta"))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isEmpty())
               .andExpect(status().isNoContent());

    }

    @Test
    public void testGetSuggestedMetaData_count() throws Exception {
        when(suggestedNameRepository.count()).thenReturn(2L);
        mockMvc.perform(get("/v1/suggestions/meta?count=true"))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.count",is("2")))
               .andExpect(status().isOk());

    }

    @Test
    public void testSuggestName() throws Exception {
        SuggestedName suggestedName = new SuggestedName("test", "this is a test",
                                                        Arrays.asList(new GeoLocation("ABEOKUTA", "NWY")),
                                                        "test@email.com");
        String requestJson = new ObjectMapper().writeValueAsString(suggestedName);
        mockMvc.perform(post("/v1/suggestions")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isCreated());

        verify(suggestedNameRepository).save(isA(SuggestedName.class));
    }


    @Test
    public void testSuggestName_invalid_email() throws Exception {
        SuggestedName suggestedName = new SuggestedName("test", "this is a test",
                                                        Arrays.asList(new GeoLocation("ABEOKUTA", "NWY")),
                                                        "testemail.com");
        String requestJson = new ObjectMapper().writeValueAsString(suggestedName);
        mockMvc.perform(post("/v1/suggestions")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest());

        verify(suggestedNameRepository, never()).save(isA(SuggestedName.class));
    }

    @Test
    public void testSuggestName_invalid_name() throws Exception {
        SuggestedName suggestedName = new SuggestedName("", "this is a test",
                                                        Arrays.asList(new GeoLocation("ABEOKUTA", "NWY")),
                                                        "test@email.com");
        String requestJson = new ObjectMapper().writeValueAsString(suggestedName);
        mockMvc.perform(post("/v1/suggestions")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest());

        verify(suggestedNameRepository, never()).save(isA(SuggestedName.class));
    }

    @Test
    public void testGetAllSuggestedNames() throws Exception {
        mockMvc.perform(get("/v1/suggestions"))
               .andExpect(status().isOk());
        verify(suggestedNameRepository).findAll();
    }

    @Test
    public void testDeleteSuggestedName() throws Exception {
        final String testName = "lagbaja";
        SuggestedName suggestedName = mock(SuggestedName.class);
        when(suggestedNameRepository.findByName(testName)).thenReturn(suggestedName);
        mockMvc.perform(delete("/v1/suggestions/{name}", testName))
               .andExpect(status().isNoContent());
        verify(suggestedNameRepository).delete(isA(SuggestedName.class));
    }

    @Test
    public void testDeleteSuggestedName_name_not_found() throws Exception {
        final String testName = "lagbaja";
        when(suggestedNameRepository.findByName(testName)).thenReturn(null);
        mockMvc.perform(delete("/v1/suggestions/{name}", testName))
               .andExpect(status().isBadRequest());
        verify(suggestedNameRepository, never()).delete(isA(SuggestedName.class));
    }
}