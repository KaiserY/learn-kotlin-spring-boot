package com.kaisery.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.Id
import javax.persistence.Version

@JsonIgnoreProperties(value = "handler")
open class User() {
    constructor(name: String, password: String) : this() {
        this.name = name;
        this.password = password;
    }

    @Id
    private var id: Long? = null;

    @Version
    private var version: Long? = null;

    open var name: String = "";

    open var password: String = "";
}
