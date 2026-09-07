package Ir.co.tfs.farazaman.realtime

import java.util.Date
import java.util.UUID

class MobileChatMessage {
    var id: UUID? = null
    var senderUserName: String = ""
    var senderDisplayName: String = ""
    var recipientUserName: String = ""
    var text: String = ""
    var sentAtUtc: Date? = null
}

class LocationRequest {
    var requestId: UUID? = null
    var requesterUserName: String = ""
    var targetUserName: String = ""
    var requestedAtUtc: Date? = null
    var expiresAtUtc: Date? = null
}

class LocationResponse {
    var requestId: UUID? = null
    var responderUserName: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var accuracyMeters: Double? = null
    var altitudeMeters: Double? = null
    var speedMetersPerSecond: Double? = null
    var capturedAtUtc: Date? = null
}

class LocationRejected {
    var requestId: UUID? = null
    var responderUserName: String = ""
    var reason: String = ""
    var rejectedAtUtc: Date? = null
}

class OnlineMobileUser {
    var userName: String = ""
    var displayName: String = ""
    var clientType: String = ""
    var connectionCount: Int = 0
    var connectedAtUtc: Date? = null
}
