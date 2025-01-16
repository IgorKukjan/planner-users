package ru.javabegin.micro.planner.users.repo

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.javabegin.micro.planner.entity.User


// принцип ООП: абстракция-реализация - здесь описываем все доступные способы доступа к данным
@Repository
interface UserRepository : JpaRepository<User, Long?> {
    fun findByEmail(email: String): User?


    fun deleteByEmail(email: String) // строгое соотвествие email (не вхождени)


    @Query(
        ("SELECT u FROM User u where " +
                "(:email is null or :email='' or lower(u.email) like lower(concat('%', :email,'%'))) and" +
                " (:username is null or :username='' or lower(u.username) like lower(concat('%', :username,'%')))")
    )
    fun findByParams(
        @Param("username") username: String?,
        @Param("email") email: String,
        pageable: Pageable
    ): Page<User?>
}
