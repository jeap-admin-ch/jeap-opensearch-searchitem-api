package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.indextype.Origin;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.AmbiguousIndexTypeException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemsBadInputException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.UnsupportedIndexMajorVersionException;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentDataV1;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentDataV2;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import ch.admin.bit.jeap.security.test.resource.JeapAuthenticationTestTokenBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchItemsController.class)
@ContextConfiguration(classes = OpenSearchSearchItemApiTestConfig.class)
@AutoConfigureMockMvc
class SearchItemsControllerIndexMajorVersionTest {

    private static final String INDEX_TYPE = "TestDocument";
    private static final String ORIGIN_ID = "12345";
    private static final Origin ORIGIN = new Origin(ORIGIN_ID, "business-revision-7", null, null, null, null, null);

    private static final SemanticApplicationRole READ_ROLE = SemanticApplicationRole.builder()
            .system("jme").resource("searchitem").operation("read").build();
    private static final SemanticApplicationRole FOO_ROLE = SemanticApplicationRole.builder()
            .system("jme").resource("searchitem").operation("foo").build();

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SearchItemsProvider searchItemsProvider;
    @MockitoSpyBean
    private IndexTypesUtils indexTypesUtils;

    @Test
    void returnsCurrentObjectBuiltForMajor1() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenReturn(Optional.of(new SearchItemContainer(1, 3, new SearchItem<>(ORIGIN, new TestDocumentDataV1("Decree")))));

        mockMvc.perform(request(1))
                .andExpect(status().isOk())
                .andExpect(header().string("index-type", INDEX_TYPE))
                .andExpect(header().string("index-major-version", "1"))
                .andExpect(header().string("index-minor-version", "3"))
                .andExpect(jsonPath("$.origin.id").value(ORIGIN_ID))
                .andExpect(jsonPath("$.data.title").value("Decree"))
                .andExpect(jsonPath("$.data.language").doesNotExist());

        verify(searchItemsProvider, never()).findSearchItem(any(), any(), any());
    }

    @Test
    void returnsSameCurrentObjectBuiltForMajor2() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 2))
                .thenReturn(Optional.of(new SearchItemContainer(2, 0, new SearchItem<>(ORIGIN, new TestDocumentDataV2("Decree", "de")))));

        mockMvc.perform(request(2))
                .andExpect(status().isOk())
                .andExpect(header().string("index-type", INDEX_TYPE))
                .andExpect(header().string("index-major-version", "2"))
                .andExpect(header().string("index-minor-version", "0"))
                .andExpect(jsonPath("$.data.title").value("Decree"))
                .andExpect(jsonPath("$.data.language").value("de"));
    }

    @Test
    void returnsUnprocessableContent_whenMajorNotRegisteredForIndexType() throws Exception {
        mockMvc.perform(request(3))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_INDEX_MAJOR_VERSION"))
                .andExpect(jsonPath("$.index_type").value(INDEX_TYPE))
                .andExpect(jsonPath("$.index_major_version").value(3));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsBadRequest_whenIndexTypeIsNotRegistered() throws Exception {
        mockMvc.perform(get("/index-api/searchitems")
                        .param("index_type", "Unknown")
                        .param("origin_id", ORIGIN_ID)
                        .param("index_major_version", "1")
                        .with(authentication(token(READ_ROLE))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNKNOWN_INDEX_TYPE"))
                .andExpect(jsonPath("$.index_type").value("Unknown"));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsBadRequestWithoutBody_whenProviderRejectsInput() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenThrow(new SearchItemsBadInputException("invalid origin id"));

        mockMvc.perform(request(1))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void returnsUnprocessableContent_whenProviderCannotBuildMajor() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenThrow(new UnsupportedIndexMajorVersionException(INDEX_TYPE, 1));

        mockMvc.perform(request(1))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_INDEX_MAJOR_VERSION"));
    }

    @Test
    void returnsNotFound_whenObjectDoesNotExist() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1)).thenReturn(Optional.empty());

        mockMvc.perform(request(1))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsServerError_whenProviderReturnsOtherMajor() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenReturn(Optional.of(new SearchItemContainer(2, 0, new SearchItem<>(ORIGIN, new TestDocumentDataV2("Decree", "de")))));

        mockMvc.perform(request(1))
                .andExpect(status().isInternalServerError())
                .andExpect(header().doesNotExist("index-major-version"))
                .andExpect(jsonPath("$.code").value("SEARCH_ITEM_MISMATCH"));
    }

    @Test
    void returnsServerError_whenPayloadDoesNotMatchRequestedMajor() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenReturn(Optional.of(new SearchItemContainer(1, 3, new SearchItem<>(ORIGIN, new TestDocumentDataV2("Decree", "de")))));

        mockMvc.perform(request(1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("urn:jeap:opensearch:searchitem:search-item-mismatch"))
                .andExpect(jsonPath("$.code").value("SEARCH_ITEM_MISMATCH"))
                .andExpect(jsonPath("$.index_type").value(INDEX_TYPE))
                .andExpect(jsonPath("$.index_major_version").value(1))
                .andExpect(jsonPath("$.detail").value(not(containsString(TestDocumentDataV2.class.getName()))));
    }

    @Test
    void returnsServerError_whenMinorIsUnknownToIndexType() throws Exception {
        when(searchItemsProvider.findCurrentSearchItem(INDEX_TYPE, ORIGIN_ID, 1))
                .thenReturn(Optional.of(new SearchItemContainer(1, 4, new SearchItem<>(ORIGIN, new TestDocumentDataV1("Decree")))));

        mockMvc.perform(request(1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("SEARCH_ITEM_MISMATCH"));
    }

    @Test
    void returnsBadRequest_whenCombinedWithOriginVersion() throws Exception {
        mockMvc.perform(request(1).param("origin_version", "business-revision-7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INDEX_MAJOR_VERSION"));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsBadRequest_whenMajorIsNotANumber() throws Exception {
        mockMvc.perform(get("/index-api/searchitems")
                        .param("index_type", INDEX_TYPE)
                        .param("origin_id", ORIGIN_ID)
                        .param("index_major_version", "v1")
                        .with(authentication(token(READ_ROLE))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INDEX_MAJOR_VERSION"));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsBadRequest_whenMajorIsEmpty() throws Exception {
        mockMvc.perform(get("/index-api/searchitems")
                        .param("index_type", INDEX_TYPE)
                        .param("origin_id", ORIGIN_ID)
                        .param("index_major_version", "")
                        .with(authentication(token(READ_ROLE))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INDEX_MAJOR_VERSION"));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsServerError_whenIndexTypeIsAmbiguous() throws Exception {
        doThrow(new AmbiguousIndexTypeException(INDEX_TYPE, 1, 2)).when(indexTypesUtils).findIndexType(INDEX_TYPE, 1);

        mockMvc.perform(request(1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("AMBIGUOUS_INDEX_TYPE"))
                .andExpect(jsonPath("$.index_type").value(INDEX_TYPE))
                .andExpect(jsonPath("$.index_major_version").value(1));

        verifyNoInteractions(searchItemsProvider);
    }

    @Test
    void returnsForbidden_withoutReadRole() throws Exception {
        mockMvc.perform(get("/index-api/searchitems")
                        .param("index_type", INDEX_TYPE)
                        .param("origin_id", ORIGIN_ID)
                        .param("index_major_version", "1")
                        .with(authentication(token(FOO_ROLE))))
                .andExpect(status().isForbidden());

        verify(searchItemsProvider, never()).findCurrentSearchItem(any(), any(), anyInt());
    }

    @Test
    void returnsUnauthorized_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/index-api/searchitems")
                        .param("index_type", INDEX_TYPE)
                        .param("origin_id", ORIGIN_ID)
                        .param("index_major_version", "1"))
                .andExpect(status().isUnauthorized());

        verify(searchItemsProvider, never()).findCurrentSearchItem(any(), any(), anyInt());
    }

    private MockHttpServletRequestBuilder request(int indexMajorVersion) {
        return get("/index-api/searchitems")
                .param("index_type", INDEX_TYPE)
                .param("origin_id", ORIGIN_ID)
                .param("index_major_version", String.valueOf(indexMajorVersion))
                .accept(MediaType.APPLICATION_JSON)
                .with(authentication(token(READ_ROLE)));
    }

    private static JeapAuthenticationToken token(SemanticApplicationRole... roles) {
        return JeapAuthenticationTestTokenBuilder.create().withUserRoles(roles).build();
    }
}
