package comtest.ct.cd.purwo.rest

import com.google.gson.annotations.SerializedName

/**
 * Created By Purwo on 12/09/20
 */

data class UserResponse(
    @SerializedName("total_count")
    val totalCount: Int?,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean?,
    val items: List<UserModel>
)