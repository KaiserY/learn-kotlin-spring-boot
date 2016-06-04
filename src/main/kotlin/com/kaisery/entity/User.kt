package com.kaisery.entity

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "kotlin_user")
data class User(

        @Id
        @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
        var id: Long = 0,

        var name: String = "",

        var password: String = ""
) : Serializable {
    constructor() : this(0, "", "")
}
