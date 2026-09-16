import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {
    def messageLog = messageLogFactory.getMessageLog(message)
    def body = message.getBody(String.class) ?: ""
    def properties = message.getProperties()
    def headers = message.getHeaders()

    // ---- Pull runtime/error context dynamically ----
    // Calling iFlows set these as exchange properties (via Content Modifier,
    // typically in the Exception Subprocess) before routing here via
    // Process Direct. Falls back to headers, then a safe default, so a
    // missing property never breaks the send.
    def getVal = { String key, String fallback = "N/A" ->
        def v = properties.get(key) ?: headers.get(key)
        return v ? v.toString() : fallback
    }

    def source         = getVal("ErrorSource")
    def target          = getVal("ErrorTarget")
    def runtimeName     = getVal("ErrorRuntimeName")
    def executionId     = getVal("ErrorExecutionId", headers.get("SAP_MessageProcessingLogID")?.toString() ?: "N/A")
    def correlationId   = getVal("ErrorCorrelationId", headers.get("SAP_MessageCorrelationId")?.toString() ?: "N/A")
    def processName     = getVal("ErrorProcessName", "Integration Process")
    def errorMessage    = getVal("ErrorMessage", body ?: "No error details provided")
    def recipientEmail  = getVal("ErrorRecipientEmail", "shivamkumar.singh@techygeekhub.com")
    def senderEmail      = getVal("ErrorSenderEmail", "shivamsinghyadav070@gmail.com")
    def errorTimestamp  = new Date().format("yyyyMMdd HHmmss.SSS")

    // ---- Attachment: the original message body ----
    // Filename/content-type are overridable per calling iFlow via properties,
    // so an XML payload, JSON payload, CSV, etc. all attach correctly.
    def attachmentFileName = getVal("ErrorAttachmentFileName", "Payload_${executionId}.xml")
    def attachmentType     = getVal("ErrorAttachmentType", "application/xml")
    def attachmentContent  = body ? body.getBytes("UTF-8").encodeBase64().toString() : null

    // ---- Build HTML body matching your sample template ----
    def htmlBody = """
    <html>
    <body style="font-family: Arial, sans-serif; font-size: 13px; color: #333;">
        <p>Dear Stakeholder,</p>
        <p>An error occurred during the execution of the <b>${processName}</b> process.
        Please find the details of the failed record below:</p>
        <table border="1" cellpadding="8" cellspacing="0"
               style="border-collapse: collapse; width: 100%;">
            <tr style="background-color:#f2f2f2;">
                <th>Source</th>
                <th>Target</th>
                <th>RuntimeName</th>
                <th>ExecutionId</th>
                <th>ErrorTimestamp</th>
                <th>ErrorMessage</th>
                <th>CorrelationID</th>
            </tr>
            <tr>
                <td>${source}</td>
                <td>${target}</td>
                <td>${runtimeName}</td>
                <td>${executionId}</td>
                <td>${errorTimestamp}</td>
                <td>${errorMessage}</td>
                <td>${correlationId}</td>
            </tr>
        </table>
        <p><b>Recommended Action:</b><br/>
        Please review the error message and investigate the root cause.
        If assistance is required, contact the integration operations team
        with the ExecutionID for expedited resolution.</p>
    </body>
    </html>
    """.stripIndent()

    // ---- Build SendGrid payload ----
    def payload = [
        personalizations: [[ to: [[ email: recipientEmail ]] ]],
        from: [ email: senderEmail ],
        subject: "Error - ${processName} : ${errorTimestamp}",
        content: [[ type: "text/html", value: htmlBody ]]
    ]

    // Only attach if there's an actual body to send — avoids SendGrid
    // rejecting the payload for an empty/invalid attachment block.
    if (attachmentContent) {
        payload.attachments = [[
            content: attachmentContent,
            type: attachmentType,
            filename: attachmentFileName,
            disposition: "attachment"
        ]]
    }

    message.setBody(JsonOutput.toJson(payload))
    message.setHeader("Content-Type", "application/json")

    return message
}