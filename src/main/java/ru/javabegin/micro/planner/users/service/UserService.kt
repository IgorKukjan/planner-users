package ru.javabegin.micro.planner.users.service

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.javabegin.micro.planner.entity.User
import ru.javabegin.micro.planner.users.repo.UserRepository
import java.util.*

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что мало методов или это все можно реализовать сразу в контроллере
// Такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями)
@Service // все методы класса должны выполниться без ошибки, чтобы транзакция завершилась
// если в методе возникнет исключение - все выполненные операции откатятся (Rollback)
@Transactional
class UserService(// сервис имеет право обращаться к репозиторию (БД)
    private val repository: UserRepository
) {
    // возвращает только либо 0 либо 1 объект, т.к. email уникален для каждого пользователя
    fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }

    fun add(user: User): User {
        return repository.save(user) // метод save обновляет или создает новый объект, если его не было
    }

    fun update(user: User): User {
        return repository.save(user) // метод save обновляет или создает новый объект, если его не было
    }

    fun deleteByUserId(id: Long) {
        repository.deleteById(id)
    }

    fun deleteByUserEmail(email: String) {
        repository.deleteByEmail(email)
    }

    //    public User findById(Long id) {
    //        return repository.findById(id).get(); // т.к. возвращается Optional - можно получить объект методом get()
    //    }
    fun findById(id: Long): Optional<User> {
        return repository.findById(id)
    }

    fun findByParams(username: String?, email: String, paging: PageRequest): Page<User?> {
        return repository.findByParams(username, email, paging)
    }
}
