package org.taymyr.lagom.openapi.autogenerate;

import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.typesafe.config.ConfigFactory.empty;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.taymyr.lagom.javadsl.api.transport.MessageProtocols.YAML;
import static org.taymyr.lagom.openapi.TestUtils.eventually;
import static org.taymyr.lagom.openapi.TestUtils.resourceAsString;
import static org.taymyr.lagom.openapi.TestUtils.yamlToJson;

class AutogeneratedOpenAPIServiceTest {

    @Test
    @DisplayName("Service without OpenAPIDefinition annotation should return 404")
    void shouldReturn404WithoutAnnotation() {
        EmptyTestServiceImpl service = new EmptyTestServiceImpl(empty());
        assertThatExceptionOfType(NotFound.class)
            .isThrownBy(() -> service.openapi().invokeWithHeaders(null, null))
            .withMessage("OpenAPI specification not found")
            .has(new Condition<>(thr -> thr.errorCode().http() == 404, "Error code must be 404"));
    }

    @Test
    @DisplayName("Test autogenerate yaml specification")
    void shouldNormalGenerateYaml() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        String expected = yamlToJson(resourceAsString("autogenerated.yml"));
        TestServiceImpl service = new TestServiceImpl(empty());
        Pair<ResponseHeader, String> openapi = eventually(service.openapi().invokeWithHeaders(null, null));
        assertThat(openapi.first().status()).isEqualTo(200);
        assertThat(openapi.first().protocol()).isEqualTo(YAML);
        assertThatJson(yamlToJson(openapi.second())).isEqualTo(expected);
    }

}
