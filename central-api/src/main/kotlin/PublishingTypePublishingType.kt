import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class PublishingTypePublishingType {
  @SerialName(value = "USER_MANAGED")
  UserManaged,
  @SerialName(value = "AUTOMATIC")
  Automatic,
}
