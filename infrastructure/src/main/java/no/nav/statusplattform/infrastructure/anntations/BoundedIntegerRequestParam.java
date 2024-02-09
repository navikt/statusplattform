package no.nav.statusplattform.infrastructure.anntations;

import org.actioncontroller.ApiControllerContext;
import org.actioncontroller.meta.HttpClientParameterMapper;
import org.actioncontroller.meta.HttpParameterMapper;
import org.actioncontroller.meta.HttpParameterMapperFactory;
import org.actioncontroller.meta.HttpParameterMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Maps the HTTP request parameter to the parameter, converting the type if necessary.
 *
 * @see HttpParameterMapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@HttpParameterMapping(BoundedIntegerRequestParam.MapperFactory.class)
public @interface BoundedIntegerRequestParam {

   String paramName();
   int defaultValue() default 1;
   int lowerBound() default 1;
   int upperBound() default Integer.MAX_VALUE;

   class MapperFactory implements HttpParameterMapperFactory<BoundedIntegerRequestParam> {
      @Override
      public HttpParameterMapper create(BoundedIntegerRequestParam annotation, Parameter parameter, ApiControllerContext context) {
         if (parameter.getType() != Integer.class && parameter.getType() != int.class) {
            throw new IllegalArgumentException("This annotation requires the parameter to be Integer");
         }
         String name = annotation.paramName();
         return (exchange) -> getValueWithinBounds(annotation, Optional.ofNullable(exchange.getParameter(name)).map(Integer::parseInt));
      }

      private Integer getValueWithinBounds(BoundedIntegerRequestParam annotation, Optional<Integer> parameter) {
         int value = parameter.orElse(annotation.defaultValue());
         int lowerBound = annotation.lowerBound();
         int upperBound = annotation.upperBound();

         if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException("Value: " + value + " is not within bounds. Lower bound: " + lowerBound + ". Upper bound: " + upperBound);
         }
         return value;
      }

      @Override
      public HttpClientParameterMapper clientParameterMapper(BoundedIntegerRequestParam annotation, Parameter parameter) {
         String name = annotation.paramName();
         return (exchange, arg) -> exchange.setRequestParameter(name, arg);
      }
   }
}
