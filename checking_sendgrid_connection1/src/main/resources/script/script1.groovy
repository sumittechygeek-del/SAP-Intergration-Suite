import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message, com.sap.gateway.ip.core.customdev.util.MessageLogFactory messageLogFactory) {
    def messageLog = messageLogFactory.getMessageLog(message)
    
    // your existing logic, e.g. reading the secure store credential
    import com.sap.it.api.securestore.SecureStoreService
    import com.sap.it.api.ITApiFactory
    
    def service = ITApiFactory.getApi(SecureStoreService.class, null)
    def credential = service.getUserCredential("SendGridAPIKey")
    def apiKey = new String(credential.getPassword())
    
    message.setHeader("Authorization", "Bearer " + apiKey)
    return message
}