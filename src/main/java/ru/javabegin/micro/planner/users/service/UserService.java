package ru.javabegin.micro.planner.users.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.Task;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.users.repo.UserRepository;

import jakarta.transaction.Transactional;
import java.util.Date;

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что мало методов или это все можно реализовать сразу в контроллере
// Такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями)
@Service

// все методы класса должны выполниться без ошибки, чтобы транзакция завершилась
// если в методе возникнет исключение - все выполненные операции откатятся (Rollback)
@Transactional
public class UserService {

    private final UserRepository repository; // сервис имеет право обращаться к репозиторию (БД)

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // возвращает только либо 0 либо 1 объект, т.к. email уникален для каждого пользователя
    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User add(User user) {
        return repository.save(user); // метод save обновляет или создает новый объект, если его не было
    }

    public User update(User user) {
        return repository.save(user); // метод save обновляет или создает новый объект, если его не было
    }

    public void deleteByUserId(Long id) {
        repository.deleteById(id);
    }

    public void deleteByUserEmail(String email) {
        repository.deleteByEmail(email);
    }

    public User findById(Long id) {
        return repository.findById(id).get(); // т.к. возвращается Optional - можно получить объект методом get()
    }

    public Page<User> findByParams(String username, String password, PageRequest paging) {
        return repository.findByParams(username, password, paging);
    }

}
