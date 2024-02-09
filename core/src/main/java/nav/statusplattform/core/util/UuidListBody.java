package nav.statusplattform.core.util;

import org.actioncontroller.ApiControllerContext;
import org.actioncontroller.meta.HttpClientParameterMapper;
import org.actioncontroller.meta.HttpParameterMapper;
import org.actioncontroller.meta.HttpParameterMapperFactory;
import org.actioncontroller.meta.HttpParameterMapping;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.pojo.JsonGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.UUID;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Retention(RUNTIME)
@Target({PARAMETER})
@HttpParameterMapping(nav.statusplattform.core.util.UuidListBody.MapperFactory.class)
public @interface UuidListBody {
        class MapperFactory implements HttpParameterMapperFactory<nav.statusplattform.core.util.UuidListBody>{

            @Override
            public HttpParameterMapper create(nav.statusplattform.core.util.UuidListBody annotation, Parameter parameter, ApiControllerContext context){
                return exchange ->  JsonArray.read(exchange.getReader()).mapNodes(jsonNode -> UUID.fromString(jsonNode.stringValue()));
            }

            @Override
            public HttpClientParameterMapper clientParameterMapper(nav.statusplattform.core.util.UuidListBody annotation, Parameter parameter){
                return (exchange, o) -> exchange.write("application/json", writer -> JsonGenerator.generate(o).toJson(writer));
            }
        }
}
