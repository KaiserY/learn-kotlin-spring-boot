package com.kaisery.shiro

import com.hazelcast.query.Predicate
import org.apache.shiro.session.Session
import java.io.Serializable

class SessionAttributePredicate<T>(val attributeName: String, val attributeValue: T) : Predicate<Serializable, Session> {

    override fun apply(sessionEntry: Map.Entry<Serializable, Session>): Boolean {
        return sessionEntry.value.getAttribute(attributeName) == attributeValue
    }

}
