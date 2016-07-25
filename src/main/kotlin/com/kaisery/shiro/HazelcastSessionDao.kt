package com.kaisery.shiro

import com.hazelcast.config.*
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.IMap
import com.kaisery.common.logging.Logging
import org.apache.shiro.session.Session
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO
import java.io.Serializable
import java.util.*

class HazelcastSessionDao() : AbstractSessionDAO() {

    val hcInstanceName: String = UUID.randomUUID().toString()
    private var map: IMap<Serializable, Session>? = null

    private val HC_MAP = "sessions"
    private val HC_GROUP_NAME = "hc"
    private val HC_GROUP_PASSWORD = "oursessionssecret"
    private val HC_PORT = 5701
    private val HC_MULTICAST_GROUP = "224.2.2.3"
    private val HC_MULTICAST_PORT = 54327

    init {
        Logging.logger.info("Initializing Hazelcast Shiro session persistence..")

        // configure Hazelcast instance
        val cfg = Config()
        cfg.setInstanceName(hcInstanceName)
        // group configuration
        cfg.setGroupConfig(GroupConfig(HC_GROUP_NAME, HC_GROUP_PASSWORD))
        // network configuration initialization
        val netCfg = NetworkConfig()
        netCfg.setPortAutoIncrement(true)
        netCfg.setPort(HC_PORT)
        // multicast
        val mcCfg = MulticastConfig()
        mcCfg.setEnabled(false)
        mcCfg.setMulticastGroup(HC_MULTICAST_GROUP)
        mcCfg.setMulticastPort(HC_MULTICAST_PORT)
        // tcp
        val tcpCfg = TcpIpConfig()
        tcpCfg.addMember("127.0.0.1")
        tcpCfg.setEnabled(false)
        // network join configuration
        val joinCfg = JoinConfig()
        joinCfg.setMulticastConfig(mcCfg)
        joinCfg.setTcpIpConfig(tcpCfg)
        netCfg.setJoin(joinCfg)
        // ssl
        netCfg.setSSLConfig(SSLConfig().setEnabled(false))

        // get map
        this.map = Hazelcast.newHazelcastInstance(cfg).getMap(HC_MAP)
        Logging.logger.info("Hazelcast Shiro session persistence initialized.")
    }

    override fun update(session: Session?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveSessions(): MutableCollection<Session>? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doReadSession(sessionId: Serializable?): Session {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doCreate(session: Session?): Serializable {
        val sessionId: Serializable = generateSessionId(session)
        Logging.logger.debug("Creating a new session identified by[{}]", sessionId);
        assignSessionId(session, sessionId);
        this.map!!.put(session?.getId(), session);

        return sessionId;
    }

    override fun delete(session: Session?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getSessionsForAuthenticationEntity(email: String): Collection<Session> {
        Logging.logger.debug("Looking up for sessions related to [{}]", email)
        val predicate = SessionAttributePredicate("email", email)
        return map!!.values(predicate)
    }

    fun destroy() {
        Logging.logger.info("Shutting down Hazelcast instance [{}]..", hcInstanceName)
        val instance = Hazelcast.getHazelcastInstanceByName(
            hcInstanceName)
        instance?.shutdown()
    }
}
