package dev.magadiflo.course.app.mapper;

import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.entity.CourseUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseUserMapper {

    /**
     * Convierte la respuesta del servicio externo en una entidad local.
     *
     * @param userResponse DTO proveniente del user-service (puerto 8001).
     * @return Entidad CourseUser lista para ser asociada a un curso en PostgreSQL (puerto 8002).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "id")
    CourseUser toCourseUser(UserResponse userResponse);

}
