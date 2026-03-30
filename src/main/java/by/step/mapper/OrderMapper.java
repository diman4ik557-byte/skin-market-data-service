package by.step.mapper;

import by.step.dto.OrderDto;
import by.step.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.username", target = "customerName")
    @Mapping(source = "artist.id", target = "artistId")
    @Mapping(source = "artist.username", target = "artistName")
    OrderDto toDto(Order order);

    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "artistId", target = "artist.id")
    Order toEntity(OrderDto orderDto);
}