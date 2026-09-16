import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.securestore.SecureStoreService
import com.sap.it.api.ITApiFactory

def Message processData(Message message) {
    def service = ITApiFactory.getApi(SecureStoreService.class, null)
    def credential = service.getUserCredential("sendgrid_apikey")
    def apiKey = new String(credential.getPassword())
    
    message.setHeader("Authorization", "Bearer " + apiKey)
    return message
}