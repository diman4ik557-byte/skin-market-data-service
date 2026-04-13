package by.step.mapper;

import by.step.dto.OrderDto;
import by.step.entity.Order;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T21:50:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDto toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDto.OrderDtoBuilder orderDto = OrderDto.builder();

        orderDto.customerId( orderCustomerId( order ) );
        orderDto.customerName( orderCustomerUsername( order ) );
        orderDto.artistId( orderArtistId( order ) );
        orderDto.artistName( orderArtistUsername( order ) );
        orderDto.id( order.getId() );
        orderDto.status( order.getStatus() );
        orderDto.description( order.getDescription() );
        orderDto.price( order.getPrice() );
        orderDto.finalFileUrl( order.getFinalFileUrl() );
        orderDto.createdAt( order.getCreatedAt() );
        orderDto.completedAt( order.getCompletedAt() );

        return orderDto.build();
    }

    @Override
    public Order toEntity(OrderDto orderDto) {
        if ( orderDto == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.customer( orderDtoToUser( orderDto ) );
        order.artist( orderDtoToUser1( orderDto ) );
        order.id( orderDto.getId() );
        order.status( orderDto.getStatus() );
        order.description( orderDto.getDescription() );
        order.price( orderDto.getPrice() );
        order.finalFileUrl( orderDto.getFinalFileUrl() );
        order.createdAt( orderDto.getCreatedAt() );
        order.completedAt( orderDto.getCompletedAt() );

        return order.build();
    }

    private Long orderCustomerId(Order order) {
        if ( order == null ) {
            return null;
        }
        User customer = order.getCustomer();
        if ( customer == null ) {
            return null;
        }
        Long id = customer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderCustomerUsername(Order order) {
        if ( order == null ) {
            return null;
        }
        User customer = order.getCustomer();
        if ( customer == null ) {
            return null;
        }
        String username = customer.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private Long orderArtistId(Order order) {
        if ( order == null ) {
            return null;
        }
        User artist = order.getArtist();
        if ( artist == null ) {
            return null;
        }
        Long id = artist.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderArtistUsername(Order order) {
        if ( order == null ) {
            return null;
        }
        User artist = order.getArtist();
        if ( artist == null ) {
            return null;
        }
        String username = artist.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    protected User orderDtoToUser(OrderDto orderDto) {
        if ( orderDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( orderDto.getCustomerId() );

        return user.build();
    }

    protected User orderDtoToUser1(OrderDto orderDto) {
        if ( orderDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( orderDto.getArtistId() );

        return user.build();
    }
}
