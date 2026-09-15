import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {
    def errorText = message.getBody(String.class)

    def payload = [
        personalizations: [[ to: [[ email: "shivamkumar.singh@techygeekhub.com" ]] ]],
        from: [ email: "shivamsinghyadav070@gmail.com" ],
        subject: "SAP Process Error",
        content: [[ type: "text/plain", value: errorText ]],
        attachments: [[
            content: "VGhpcyBpcyBhIHRlc3QgYXR0YWNobWVudA==",
            type: "text/csv",
            filename: "ErrorReport.csv",
            disposition: "attachment"
        ]]
    ]

    message.setBody(JsonOutput.toJson(payload))
    message.setHeader("Content-Type", "application/json")

    return message
}