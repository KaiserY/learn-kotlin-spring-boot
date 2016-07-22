package com.kaisery.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.orient.`object`.OrientObjectDatabaseFactory
import org.springframework.data.orient.`object`.repository.support.OrientObjectRepositoryFactoryBean
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Configuration
@EnableOrientRepositories(basePackages = arrayOf("com.kaisery.repository"), repositoryFactoryBeanClass = OrientObjectRepositoryFactoryBean::class)
open class OrientDbConfiguration @Autowired constructor(var factory: OrientObjectDatabaseFactory) {

    @PostConstruct
    @Transactional
    open fun registerEntities() {
        factory.db().entityManager.registerEntityClasses("com.kaisery.entity");
    }
}
