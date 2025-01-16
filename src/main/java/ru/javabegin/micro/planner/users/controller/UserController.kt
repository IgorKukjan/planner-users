package ru.javabegin.micro.planner.users.controller

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.javabegin.micro.planner.entity.User
import ru.javabegin.micro.planner.users.mq.func.MessageFuncActions
import ru.javabegin.micro.planner.users.search.UserSearchValues
import ru.javabegin.micro.planner.users.service.UserService
import ru.javabegin.micro.planner.utils.rest.webclient.UserWebClientBuilder
import java.text.ParseException

/*

Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные, а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/
@RestController
@RequestMapping("/user") // базовый URI
class UserController // используем автоматическое внедрение экземпляра класса через конструктор
// не используем @Autowired ля переменной класса, т.к. "Field injection is not recommended "
    (// сервис для доступа к данным (напрямую к репозиториям не обращаемся)
    private val userService: UserService,
    private val userWebClientBuilder: UserWebClientBuilder,
    private val messageFuncActions: MessageFuncActions
) {
    companion object {
        const val ID_COLUMN: String = "id" // имя столбца id
    }

    // добавление
    @PostMapping("/add")
    fun add(@RequestBody user: User): ResponseEntity<Any> {
        // проверка на обязательные параметры

        var user = user
        if (user.id != null && user.id != 0L) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return ResponseEntity<Any>("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение
        if (user.email == null || user.email!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: email", HttpStatus.NOT_ACCEPTABLE)
        }

        if (user.password == null || user.password!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: password", HttpStatus.NOT_ACCEPTABLE)
        }

        if (user.username == null || user.username!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: username", HttpStatus.NOT_ACCEPTABLE)
        }


        val tmpUser = userService.add(user)

        //        if(user != null){
//            userWebClientBuilder.initUserData(user.getId()).subscribe(result ->{
//                System.out.println("user populated: " + result);
//            });
//        }
        messageFuncActions.sendNewUserMessage(tmpUser.id!!)

        return ResponseEntity.ok(user) // возвращаем созданный объект со сгенерированным id
    }


    // обновление
    @PutMapping("/update")
    fun update(@RequestBody user: User): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (user.id == null || user.id == 0L) {
            return ResponseEntity<Any>("missed param: id", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение
        if (user.email == null || user.email!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: email", HttpStatus.NOT_ACCEPTABLE)
        }

        if (user.password == null || user.password!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: password", HttpStatus.NOT_ACCEPTABLE)
        }

        if (user.username == null || user.username!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: username", HttpStatus.NOT_ACCEPTABLE)
        }


        // save работает как на добавление, так и на обновление
        userService.update(user)

        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
    }


    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @PostMapping("/deletebyid")
    fun deleteByUserId(@RequestBody userId: Long): ResponseEntity<Any> {
        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус

        try {
            userService.deleteByUserId(userId)
        } catch (e: EmptyResultDataAccessException) {
            e.printStackTrace()
            return ResponseEntity<Any>("userId=$userId not found", HttpStatus.NOT_ACCEPTABLE)
        }
        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
    }

    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @PostMapping("/deletebyemail")
    fun deleteByUserEmail(@RequestBody email: String): ResponseEntity<Any> {
        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус

        try {
            userService.deleteByUserEmail(email)
        } catch (e: EmptyResultDataAccessException) {
            e.printStackTrace()
            return ResponseEntity<Any>("email=$email not found", HttpStatus.NOT_ACCEPTABLE)
        }
        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
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
    fun findById(@RequestBody id: Long): ResponseEntity<Any> {
        val userOptional = userService.findById(id)

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            if (userOptional.isPresent) { // если объект найден
                return ResponseEntity.ok(userOptional.get()) // получаем User из контейнера и возвращаем в теле ответа
            }
        } catch (e: NoSuchElementException) { // если объект не будет найден
            e.printStackTrace()
        }

        // пользователь с таким id не найден
        return ResponseEntity<Any>("id=$id not found", HttpStatus.NOT_ACCEPTABLE)
    }

    // получение уникального объекта по email
    @PostMapping("/email")
    fun findByEmail(@RequestBody email: String): ResponseEntity<Any> { // строго соответствие email

        var user: User? = null

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            user = userService.findByEmail(email)
        } catch (e: NoSuchElementException) { // если объект не будет найден
            e.printStackTrace()
            return ResponseEntity<Any>("email=$email not found", HttpStatus.NOT_ACCEPTABLE)
        }

        return ResponseEntity.ok(user)
    }


    // поиск по любым параметрам UserSearchValues
    @PostMapping("/search")
    @Throws(ParseException::class)
    fun search(@RequestBody userSearchValues: UserSearchValues): ResponseEntity<Any> {
        // все заполненные условия проверяются условием ИЛИ - это можно изменять в запросе репозитория

        // можно передавать не полный email, а любой текст для поиска

        val email = userSearchValues.email

        val username =  userSearchValues.username ?: ""

        //        // проверка на обязательные параметры - если они нужны по задаче
//        if (email == null || email.trim().length() == 0) {
//            return new ResponseEntity("missed param: user email", HttpStatus.NOT_ACCEPTABLE);
//        }
        val sortColumn = userSearchValues.sortColumn
        val sortDirection =  userSearchValues.sortDirection

        val pageNumber = userSearchValues.pageNumber
        val pageSize = userSearchValues.pageSize

        // направление сортировки
        val direction =
            if (sortDirection == null || sortDirection.trim { it <= ' ' }.length == 0 || sortDirection.trim { it <= ' ' } == "asc") Sort.Direction.ASC else Sort.Direction.DESC

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок.
            Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
            Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к. не указано второе поле сортировки.
            Поэтому и используем ID - тогда все записи с одинаковым значением приоритета будут следовать в одном порядке по ID.
         */

        // объект сортировки, который содержит стобец и направление
        val sort = Sort.by(direction, sortColumn, ID_COLUMN)

        // объект постраничности
        val pageRequest = PageRequest.of(pageNumber, pageSize, sort)

        // результат запроса с постраничным выводом
        val result = userService.findByParams(
            email,
            username, pageRequest
        )

        // результат запроса
        return ResponseEntity.ok(result)
    }


}
