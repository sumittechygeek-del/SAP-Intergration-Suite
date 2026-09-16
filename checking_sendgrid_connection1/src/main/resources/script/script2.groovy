import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {
    def toEmail = message.getProperty("ToEmail")
    def csvBytes = message.getBody(byte[].class)
    def base64Csv = csvBytes.encodeBase64().toString()

    def payload = [
        personalizations: [[ to: [[ email: toEmail ]] ]],
        from: [ email: "goyalakshat111@gmail.com" ],
        subject: "Boomi Process Error",
        content: [[ type: "text/plain", value: "The Boomi process failed." ]],
        attachments: [[
            content: base64Csv,
            type: "text/csv",
            filename: "ErrorReport.csv",
            disposition: "attachment"
        ]]
    ]

    message.setBody(JsonOutput.toJson(payload))
    message.setHeader("Content-Type", "application/json")
    return message
}