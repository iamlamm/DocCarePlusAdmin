package  com.healthtech.doccareplusadmin.domain.model

sealed class SlotAvailabilityResult {
    object Available : SlotAvailabilityResult()
    object Unavailable : SlotAvailabilityResult()
    object AlreadyBookedByCurrentUser : SlotAvailabilityResult()
    object AlreadyBookedByOther : SlotAvailabilityResult()
}