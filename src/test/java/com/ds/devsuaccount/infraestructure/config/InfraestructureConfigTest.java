package com.ds.devsuaccount.infraestructure.config;

import com.ds.devsuaccount.infraestructure.exceptions.ApiErrorResponse;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.ByteArrayJacksonJsonMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import redis.clients.jedis.Jedis;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InfraestructureConfigTest {

    @Test
    void kafkaConfigShouldBuildBeans() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "g1");

        ConsumerFactory<String, Object> factory = config.consumerFactory();
        ConcurrentKafkaListenerContainerFactory<String, Object> listenerFactory =
                config.kafkaListenerContainerFactory(factory, config.jsonMessageConverter(new JsonMapper()));

        assertNotNull(factory);
        assertEquals("localhost:9092", factory.getConfigurationProperties().get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertNotNull(listenerFactory);
    }

    @Test
    void cacheConfigShouldBuildCacheManager() {
        CacheConfig config = new CacheConfig();
        ReflectionTestUtils.setField(config, "maximumSize", 200);
        ReflectionTestUtils.setField(config, "expireAfterWrite", 5);

        assertNotNull(config.cacheManager());
    }

    @Test
    void redisConfigShouldCreateJedis() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);

        Jedis jedis = config.jedis();
        assertNotNull(jedis);
        jedis.close();
    }

    @Test
    void debugConfigShouldPrintProfilesWithoutFailing() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"local", "test"});
        DebugConfig config = new DebugConfig(env);
        config.printProfiles();
    }

    @Test
    void controllerExceptionHandlerShouldHandleKnownAndUnknownErrors() throws Exception {
        ControllerExceptionHandler handler = new ControllerExceptionHandler();

        ApiException apiException = new ApiException(ErrorCode.BAD_REQUEST);
        ResponseEntity<ApiErrorResponse> apiResponse = handler.handleApiException(apiException);
        assertEquals(400, apiResponse.getStatusCode().value());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/x/y");
        NoHandlerFoundException noHandler = new NoHandlerFoundException("GET", "/x/y", null);
        ResponseEntity<ApiErrorResponse> notFound = handler.noHandlerFoundException(request, noHandler);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());

        NoResourceFoundException noResource = mock(NoResourceFoundException.class);
        ResponseEntity<ApiErrorResponse> notFound2 = handler.noResourceFoundException(request, noResource);
        assertEquals(HttpStatus.NOT_FOUND, notFound2.getStatusCode());

        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "f1", "required")));

        ResponseEntity<Map<String, String>> validationResponse = handler.handleValidationExceptions(validationException);
        assertEquals(HttpStatus.BAD_REQUEST, validationResponse.getStatusCode());
        assertEquals("required", validationResponse.getBody().get("f1"));

        ResponseEntity<ApiErrorResponse> wrappedApi = handler.handleUnknownException(new RuntimeException(apiException));
        assertEquals(400, wrappedApi.getStatusCode().value());

        ResponseEntity<ApiErrorResponse> unknown = handler.handleUnknownException(new RuntimeException("x"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unknown.getStatusCode());
        assertTrue(unknown.getBody().getCode().contains("internal_error"));
    }

    @Test
    void kafkaConfigJsonConverterShouldUseMapper() {
        KafkaConfig config = new KafkaConfig();
        ByteArrayJacksonJsonMessageConverter converter = config.jsonMessageConverter(new JsonMapper());
        assertNotNull(converter);
    }
}
