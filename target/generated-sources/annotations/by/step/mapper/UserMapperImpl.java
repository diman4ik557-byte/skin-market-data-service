package by.step.mapper;

import by.step.dto.UserDto;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T21:50:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id( user.getId() );
        userDto.username( user.getUsername() );
        userDto.email( user.getEmail() );
        userDto.role( user.getRole() );
        userDto.balance( user.getBalance() );
        userDto.registeredAt( user.getRegisteredAt() );

        return userDto.build();
    }

    @Override
    public User toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDto.getId() );
        user.username( userDto.getUsername() );
        user.email( userDto.getEmail() );
        user.balance( userDto.getBalance() );
        user.role( userDto.getRole() );
        user.registeredAt( userDto.getRegisteredAt() );

        return user.build();
    }
}
