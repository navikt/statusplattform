package nav.portal.core.util;

import com.unboundid.util.json.JSONArray;
import org.actioncontroller.ApiControllerContext;
import org.actioncontroller.client.ApiClientExchange;
import org.actioncontroller.json.JsonBody;
import org.actioncontroller.meta.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.UUID;

import org.jsonbuddy.JsonArray;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


import org.actioncontroller.meta.HttpClientParameterMapper;
import org.actioncontroller.meta.HttpParameterMapper;
import org.actioncontroller.meta.HttpParameterMapperFactory;
import org.actioncontroller.meta.HttpParameterMapping;
import org.jsonbuddy.pojo.JsonGenerator;


@Retention(RUNTIME)
@Target({PARAMETER})
@HttpParameterMapping(nav.portal.core.util.UuidListBody.MapperFactory.class)
public @interface UuidListBody {
        class MapperFactory implements HttpParameterMapperFactory<nav.portal.core.util.UuidListBody> {

            @Override
            public HttpParameterMapper create(nav.portal.core.util.UuidListBody annotation, Parameter parameter, ApiControllerContext context) {
                return exchange ->  JsonArray.read(exchange.getReader()).mapNodes(jsonNode -> UUID.fromString(jsonNode.stringValue()));
            }

            @Override
            public HttpClientParameterMapper clientParameterMapper(nav.portal.core.util.UuidListBody annotation, Parameter parameter) {
                return (exchange, o) -> exchange.write("application/json", writer -> JsonGenerator.generate(o).toJson(writer));
            }
        }
}
