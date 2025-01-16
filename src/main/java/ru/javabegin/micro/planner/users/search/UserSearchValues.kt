package ru.javabegin.micro.planner.users.search

// возможные значения, по которым можно искать задачи + значения сортировки
class UserSearchValues (
    var email: String,
    // постраничность
    var pageNumber: Int,
    var pageSize: Int,

    // сортировка
    var sortColumn: String,
    var sortDirection: String
){
    // поля поиска (все типы - объектные, не примитивные. Чтобы можно было передать null)

     var username: String? = null


}
