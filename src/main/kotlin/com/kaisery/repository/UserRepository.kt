package com.kaisery.repository

import com.kaisery.entity.User
import org.springframework.data.orient.`object`.repository.OrientObjectRepository

interface UserRepository : OrientObjectRepository<User> {

    fun findByName(name: String): User
}
