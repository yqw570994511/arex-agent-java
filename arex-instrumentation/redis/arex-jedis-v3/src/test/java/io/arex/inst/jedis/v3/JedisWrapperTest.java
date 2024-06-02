package io.arex.inst.jedis.v3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class JedisWrapperTest {
    @Mock
    SSLSocketFactory factory;
    @Mock
    SSLParameters parameters;
    @Mock
    HostnameVerifier verifier;
    @InjectMocks
    JedisWrapper target = new JedisWrapper("", 0, 0, 0, false, factory, parameters, verifier);
    static Client client;

    @BeforeAll
    static void setUp() {
        Mockito.mockConstruction(Client.class, (mock, context) -> {
            client = mock;
        });
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        client = null;
        Mockito.clearAllCaches();
    }

    @Test
    void callWithEmptyKeysValuesReturnsDefault() {
        long result = target.msetnx( new String[]{});
        assertEquals(0, result);
    }

    @Test
    void callWithTwoKeysValuesReturnsCallableResult() {
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        Mockito.when(client.getIntegerReply()).thenReturn(1L);
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
        })) {
            long result = target.msetnx("key", "value");
            assertEquals(1L, result);

            result = target.msetnx("key1", "value1", "key2", "value2", "key3", "value3");
            assertEquals(1L, result);

            result = target.exists("key1", "key2", "key3");
            assertEquals(1L, result);
        } catch (Exception e) {
            assertThrows(NullPointerException.class, () -> {
                throw e;
            });
        }
    }

    @ParameterizedTest
    @MethodSource("callCase")
    void call(Runnable mocker, Predicate<String> predicate) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            System.out.println("mock RedisExtractor");
            Mockito.when(mock.replay()).thenReturn(MockResult.success(null));
        })) {
            String result = target.hget("key", "field");
            assertTrue(predicate.test(result));
        } catch (Exception e) {
            assertThrows(NullPointerException.class, () -> {
                throw e;
            });
        }
    }

    static Stream<Arguments> callCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(client.getBulkReply()).thenThrow(new NullPointerException());
        };
        Runnable mocker3 = () -> {
            Mockito.when(client.getBulkReply()).thenReturn("mock");
        };
        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = "mock"::equals;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate2)
        );
    }

    @Test
    void testApi() {
        assertDoesNotThrow(() -> target.expire("key".getBytes(), 1));
        assertDoesNotThrow(() -> target.append("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.substr("key".getBytes(), 1, 2));
        assertDoesNotThrow(() -> target.hset("key".getBytes(), "field".getBytes(), "value".getBytes()));
        Map<byte[], byte[]> hash = new HashMap<>();
        assertDoesNotThrow(() -> target.hset("key".getBytes(), hash));
        Map<String, String> hash1 = new HashMap<>();
        assertDoesNotThrow(() -> target.hset("key", hash1));
        assertDoesNotThrow(() -> target.hget("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.hdel("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.hvals("key".getBytes()));
        assertDoesNotThrow(() -> target.hgetAll("key".getBytes()));
        assertDoesNotThrow(() -> target.set("key", "value"));
        assertDoesNotThrow(() -> target.set("key", "value", new SetParams().ex(10)));
        assertDoesNotThrow(() -> target.get("key".getBytes()));
        assertDoesNotThrow(() -> target.exists("key".getBytes()));
        assertDoesNotThrow(() -> target.type("key".getBytes()));
        assertDoesNotThrow(() -> target.getSet("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.setnx("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.setex("key".getBytes(), 1, "value".getBytes()));
        assertDoesNotThrow(() -> target.unlink("key".getBytes()));
        assertDoesNotThrow(() -> target.ping("key".getBytes()));
    }
}
