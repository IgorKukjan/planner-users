package ru.javabegin.micro.planner.users.controller;

import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.users.dto.UserDTO;
import ru.javabegin.micro.planner.users.keycloak.KeycloakUtils;
import ru.javabegin.micro.planner.users.mq.func.MessageFuncActions;
import ru.javabegin.micro.planner.users.search.UserSearchValues;
import ru.javabegin.micro.planner.users.service.UserService;
import ru.javabegin.micro.planner.utils.rest.webclient.UserWebClientBuilder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


/*

Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные, а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/

@RestController
@RequestMapping("/admin/user") // базовый URI
public class AdminController {

    public static final String ID_COLUMN = "id"; // имя столбца id
    private final UserService userService; // сервис для доступа к данным (напрямую к репозиториям не обращаемся)
    private final KeycloakUtils keycloakUtils;

    private UserWebClientBuilder userWebClientBuilder;
    private MessageFuncActions messageFuncActions;



    // используем автоматическое внедрение экземпляра класса через конструктор
    // не используем @Autowired ля переменной класса, т.к. "Field injection is not recommended "
    public AdminController(UserService userService, UserWebClientBuilder userWebClientBuilder, MessageFuncActions messageFuncActions, KeycloakUtils keycloakUtils) {
        this.userService = userService;
        this.userWebClientBuilder = userWebClientBuilder;
        this.messageFuncActions = messageFuncActions;
        this.keycloakUtils = keycloakUtils;
    }


    // добавление
    @PostMapping("/add")
    public ResponseEntity add(@RequestBody UserDTO userDTO) {

        // проверка на обязательные параметры
        if (userDTO.getId() != null) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().length() == 0) {
            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
        }

        if (userDTO.getPassword() == null || userDTO.getPassword().trim().length() == 0) {
            return new ResponseEntity("missed param: password", HttpStatus.NOT_ACCEPTABLE);
        }

        if (userDTO.getUsername() == null || userDTO.getUsername().trim().length() == 0) {
            return new ResponseEntity("missed param: username", HttpStatus.NOT_ACCEPTABLE);
        }


//        user = userService.add(user);

//        if(user != null){
//            userWebClientBuilder.initUserData(user.getId()).subscribe(result ->{
//                System.out.println("user populated: " + result);
//            });
//        }

//        messageFuncActions.sendNewUserMessage(user.getId());

        List<String> defaultRoles = new ArrayList<>();
        defaultRoles.add("user");
        defaultRoles.add("admin");

        Response createResponse = keycloakUtils.createKeycloakUser(userDTO, defaultRoles);

        return ResponseEntity.status(createResponse.getStatus()).build(); // возвращаем созданный объект со сгенерированным id

    }


    // обновление
    @PutMapping("/update")
    public ResponseEntity update(@RequestBody UserDTO userDTO) {

        // проверка на обязательные параметры
        if (userDTO.getId().isBlank()) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение
//        if (userDTO.getEmail() == null || userDTO.getEmail().trim().length() == 0) {
//            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
//        }

//        if (userDTO.getPassword() == null || userDTO.getPassword().trim().length() == 0) {
//            return new ResponseEntity("missed param: password", HttpStatus.NOT_ACCEPTABLE);
//        }

//        if (userDTO.getUsername() == null || userDTO.getUsername().trim().length() == 0) {
//            return new ResponseEntity("missed param: username", HttpStatus.NOT_ACCEPTABLE);
//        }


        // save работает как на добавление, так и на обновление
//        userService.update(userDTO);

        // save работает как на добавление, так и на обновление
        keycloakUtils.updateKeycloakUser(userDTO);

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)

    }


    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @PostMapping("/deletebyid")
    public ResponseEntity deleteByUserId(@RequestBody String userId) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
//        try {
//            userService.deleteByUserId(userId);
//        } catch (EmptyResultDataAccessException e) {
//            e.printStackTrace();
//            return new ResponseEntity("userId=" + userId + " not found", HttpStatus.NOT_ACCEPTABLE);
//        }

        keycloakUtils.deleteKeycloakUser(userId);

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)
    }

    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @PostMapping("/deletebyemail")
    public ResponseEntity deleteByUserEmail(@RequestBody String email) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            userService.deleteByUserEmail(email);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("email=" + email + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)
    }


    // получение объекта по id
//    @PostMapping("/id")
//    public ResponseEntity<User> findById(@RequestBody Long id) {
//
//        User user = null;
//
//        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
//        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
//        try {
//            user = userService.findById(id);
//        } catch (NoSuchElementException e) { // если объект не будет найден
//            e.printStackTrace();
//            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
//        }
//
//        return ResponseEntity.ok(user);
//    }

    @PostMapping("/id")
    public ResponseEntity<UserRepresentation> findById(@RequestBody String userId) {

//        Optional<User> userOptional = userService.findById(id);

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
//        try {
//            if (userOptional.isPresent()) { // если объект найден
//                return ResponseEntity.ok(userOptional.get()); // получаем User из контейнера и возвращаем в теле ответа
//            }
//        } catch (NoSuchElementException e) { // если объект не будет найден
//            e.printStackTrace();
//        }

        // пользователь с таким id не найден
//        return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);

        return ResponseEntity.ok(keycloakUtils.findUserById(userId));
    }

    // получение уникального объекта по email
    @PostMapping("/email")
    public ResponseEntity<User> findByEmail(@RequestBody String email) { // строго соответствие email

        User user = null;

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            user = userService.findByEmail(email);
        } catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
            return new ResponseEntity("email=" + email + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(user);
    }



    // поиск по любым параметрам UserSearchValues
    @PostMapping("/search")
    public ResponseEntity<List<UserRepresentation>> search(@RequestBody String email) throws ParseException {

        // все заполненные условия проверяются условием ИЛИ - это можно изменять в запросе репозитория

        // можно передавать не полный email, а любой текст для поиска
//        String email = userSearchValues.getEmail() != null ? userSearchValues.getEmail() : null;

//        String username = userSearchValues.getUsername() != null ? userSearchValues.getUsername() : null;

//        // проверка на обязательные параметры - если они нужны по задаче
//        if (email == null || email.trim().length() == 0) {
//            return new ResponseEntity("missed param: user email", HttpStatus.NOT_ACCEPTABLE);
//        }

//        String sortColumn = userSearchValues.getSortColumn() != null ? userSearchValues.getSortColumn() : null;
//        String sortDirection = userSearchValues.getSortDirection() != null ? userSearchValues.getSortDirection() : null;

//        Integer pageNumber = userSearchValues.getPageNumber() != null ? userSearchValues.getPageNumber() : null;
//        Integer pageSize = userSearchValues.getPageSize() != null ? userSearchValues.getPageSize() : null;

        // направление сортировки
//        Sort.Direction direction = sortDirection == null || sortDirection.trim().length() == 0 || sortDirection.trim().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок.
            Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
            Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к. не указано второе поле сортировки.
            Поэтому и используем ID - тогда все записи с одинаковым значением приоритета будут следовать в одном порядке по ID.
         */

        // объект сортировки, который содержит стобец и направление
//        Sort sort = Sort.by(direction, sortColumn, ID_COLUMN);

        // объект постраничности
//        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        // результат запроса с постраничным выводом
//        Page<User> result = userService.findByParams(email, username, pageRequest);

        // результат запроса
        return ResponseEntity.ok(keycloakUtils.searchKeycloakUsers(email));

    }


}
