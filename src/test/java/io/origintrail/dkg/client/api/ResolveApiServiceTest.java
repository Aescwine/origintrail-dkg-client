package io.origintrail.dkg.client.api;

import io.origintrail.dkg.client.model.HttpUrlOptions;
import org.mockito.Mock;

import java.net.http.HttpClient;

class ResolveApiServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpUrlOptions httpUrlOptions;

    private final ResolveApiService resolveApiService = new ResolveApiService(httpClient, httpUrlOptions);

}