package com.kaisery.repository

import com.kaisery.entity.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun findByName(name: String): List<User>
}
