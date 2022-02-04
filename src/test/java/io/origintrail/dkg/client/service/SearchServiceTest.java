package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private ApiRequestService apiRequestService;

    private SearchService searchService;

    @BeforeEach
    void init() {
        searchService = new SearchService(apiRequestService);
    }

    @Test
    void entitiesSearch_requiredSearchOptionsMissing_throwsClientRequestException() {
        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> searchService.entitiesSearch(EntitySearchOptions.builder().build()).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Entity search options 'query' or 'ids' are required.");

        then(apiRequestService).shouldHaveNoInteractions();
    }

    @Test
    void entitiesSearch_searchOptionsAreNull_throwsClientRequestException() {
        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> searchService.entitiesSearch(null).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Entity search options 'query' or 'ids' are required.");

        then(apiRequestService).shouldHaveNoInteractions();
    }

    @Test
    void assertionsSearch_searchOptionsAreNull_throwsClientRequestException() {
        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> searchService.assertionsSearch(null).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Assertion search option 'query' is required.");

        then(apiRequestService).shouldHaveNoInteractions();
    }
}