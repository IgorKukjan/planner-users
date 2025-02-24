package ru.javabegin.micro.planner.users.dto;



/*

пользователь - основной объект, с которым связаны все остальные (через внешние ключи)

 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String username;
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO user = (UserDTO) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return username;
    }
}
