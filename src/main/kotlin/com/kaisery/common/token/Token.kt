package com.kaisery.common.token

import io.jsonwebtoken.Jwts

object Token {
    val builder = Jwts.builder()

    val parser = Jwts.parser()
}
