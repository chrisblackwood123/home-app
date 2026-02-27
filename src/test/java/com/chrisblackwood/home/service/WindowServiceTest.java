//package com.chrisblackwood.home.service;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.web.client.RestClient;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class WindowServiceTest {
//
//    @Test
//    void shouldReturnResponseFromWeatherApiCall() {
//        RestClient.Builder builder = mock(RestClient.Builder.class);
//        RestClient restClient = mock(RestClient.class);
//        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
//        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
//        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
//
//        when(builder.build()).thenReturn(restClient);
//        when(restClient.get()).thenReturn(uriSpec);
//        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
//        when(headersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.body(String.class)).thenReturn("weather-json");
//
//        WindowService windowService = new WindowService(builder);
//        String result = windowService.checkOvernightTemperature();
//
//        assertThat(result).isEqualTo("weather-json");
//    }
//}
