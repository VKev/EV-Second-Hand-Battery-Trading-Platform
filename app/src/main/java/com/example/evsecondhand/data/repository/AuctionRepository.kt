package com.example.evsecondhand.data.repository

import com.example.evsecondhand.data.model.AuctionDetailData
import com.example.evsecondhand.data.model.AuctionDetailResponse
import com.example.evsecondhand.data.model.AuctionLiveResponse
import com.example.evsecondhand.data.model.AuctionStatusData
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.data.model.AuctionDepositResponse
import com.example.evsecondhand.data.model.BidRequest
import com.example.evsecondhand.data.remote.AuctionApiService
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class DepositResult(
    val message: String?,
    val status: AuctionStatusData?
)

data class BidResult(
    val message: String?,
    val detail: AuctionDetailData?
)

class AuctionRepository(
    private val auctionApi: AuctionApiService
) {

    suspend fun getAuctionStatus(
        listingType: String,
        listingId: String
    ): Result<AuctionStatusData?> = runCatching {
        auctionApi.getAuctionStatus(listingType, listingId).data
    }

    suspend fun placeDeposit(
        listingType: String,
        listingId: String
    ): Result<DepositResult> = runCatching {
        val response: AuctionDepositResponse = auctionApi.placeDeposit(listingType, listingId)
        DepositResult(
            message = response.message,
            status = response.data
        )
    }

    suspend fun getLiveAuctions(time: String): Result<List<AuctionSummary>> = runCatching {
        val response: AuctionLiveResponse = auctionApi.getLiveAuctions(time)
        extractAuctionArray(response.data).mapNotNull { element ->
            parseAuctionSummary(element)
        }
    }

    suspend fun getAuctionDetail(
        listingType: String,
        listingId: String
    ): Result<AuctionDetailData?> = runCatching {
        auctionApi.getAuctionDetail(listingType, listingId).data
    }

    suspend fun placeBid(
        listingType: String,
        listingId: String,
        amount: Int
    ): Result<BidResult> = runCatching {
        val response: AuctionDetailResponse = auctionApi.placeBid(listingType, listingId, BidRequest(amount))
        BidResult(
            message = response.message,
            detail = response.data
        )
    }

    fun toAuctionSummary(detail: AuctionDetailData): AuctionSummary {
        val listingObject = detail.listing?.jsonObject
        val metadataObject = detail.metadata?.jsonObject

        val resolvedListingId = detail.listingId
            ?: listingObject?.peekString("listingId", "id")
            ?: metadataObject?.peekString("listingId", "id")
            ?: detail.id
            ?: ""

        val resolvedListingType = detail.listingType
            ?: listingObject?.peekString("listingType")
            ?: metadataObject?.peekString("listingType")
            ?: "unknown"

        val resolvedTitle = detail.title
            ?: listingObject?.peekString("title", "name")
            ?: metadataObject?.peekString("title", "name")

        val resolvedImage = detail.images?.firstOrNull()
            ?: detail.image
            ?: listingObject?.peekArrayString("images", "thumbnail", "image", "coverImage")
            ?: metadataObject?.peekArrayString("images", "thumbnail", "image", "coverImage")

        val resolvedStartingPrice = detail.startingPrice
            ?: listingObject?.peekInt("startingPrice", "price")
            ?: metadataObject?.peekInt("startingPrice", "price")

        val resolvedCurrentBid = detail.currentBid
            ?: metadataObject?.peekInt("currentBid")

        val resolvedDeposit = detail.depositAmount
            ?: metadataObject?.peekInt("depositAmount", "deposit")

        val startsAt = detail.auctionStartsAt
            ?: metadataObject?.peekString("auctionStartsAt", "startsAt")

        val endsAt = detail.auctionEndsAt
            ?: metadataObject?.peekString("auctionEndsAt", "endsAt")

        return AuctionSummary(
            id = detail.id,
            listingId = resolvedListingId,
            listingType = resolvedListingType,
            title = resolvedTitle,
            imageUrl = resolvedImage,
            startingPrice = resolvedStartingPrice,
            currentBid = resolvedCurrentBid,
            depositAmount = resolvedDeposit,
            auctionStartsAt = startsAt,
            auctionEndsAt = endsAt
        )
    }

    private fun extractAuctionArray(data: JsonElement?): List<JsonElement> {
        if (data == null || data is JsonNull) return emptyList()
        return when (data) {
            is JsonArray -> data.toList()
            is JsonObject -> when {
                "auctions" in data -> data["auctions"]?.jsonArray?.toList().orEmpty()
                else -> data.values.firstNotNullOfOrNull { value ->
                    when (value) {
                        is JsonArray -> value.toList()
                        is JsonObject -> value["auctions"]?.jsonArray?.toList()
                        else -> null
                    }
                } ?: emptyList()
            }
            else -> emptyList()
        }
    }

    private fun parseAuctionSummary(element: JsonElement): AuctionSummary? {
        val obj = element as? JsonObject ?: return null
        val listingId = obj.peekString("listingId", "listing_id", "listingID", "id") ?: return null
        val listingType = obj.peekString("listingType", "listing_type", "listingTYPE")
            ?: obj["listing"]?.jsonObject?.peekString("listingType", "listing_type")
            ?: "unknown"

        val listingObject = obj["listing"]?.jsonObject
        val metadataObject = obj["metadata"]?.jsonObject

        val title = obj.peekString("title", "name", "listingTitle")
            ?: listingObject?.peekString("title", "name")
            ?: metadataObject?.peekString("title", "name")

        val imageUrl = obj.peekArrayString("images", "thumbnail", "image", "coverImage")
            ?: listingObject?.peekArrayString("images", "thumbnail", "image", "coverImage")
            ?: metadataObject?.peekArrayString("images", "thumbnail", "image", "coverImage")

        val startingPrice = obj.peekInt("startingPrice", "starting_price", "price")
            ?: listingObject?.peekInt("startingPrice", "starting_price", "price")
            ?: metadataObject?.peekInt("startingPrice", "starting_price", "price")

        val currentBid = obj.peekInt("currentBid", "current_bid")
            ?: metadataObject?.peekInt("currentBid", "current_bid")

        val depositAmount = obj.peekInt("depositAmount", "deposit_amount")
            ?: listingObject?.peekInt("depositAmount", "deposit_amount")
            ?: metadataObject?.peekInt("depositAmount", "deposit_amount")

        val auctionStartsAt = obj.peekString("auctionStartsAt", "startsAt", "startAt")
            ?: metadataObject?.peekString("auctionStartsAt", "startsAt", "startAt")

        val auctionEndsAt = obj.peekString("auctionEndsAt", "endsAt", "endAt")
            ?: metadataObject?.peekString("auctionEndsAt", "endsAt", "endAt")

        return AuctionSummary(
            id = obj.peekString("id", "_id"),
            listingId = listingId,
            listingType = listingType,
            title = title,
            imageUrl = imageUrl,
            startingPrice = startingPrice,
            currentBid = currentBid,
            depositAmount = depositAmount,
            auctionStartsAt = auctionStartsAt,
            auctionEndsAt = auctionEndsAt
        )
    }

    private fun JsonObject.peekString(vararg keys: String): String? {
        keys.forEach { key ->
            val value = this[key].asStringOrNull()
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun JsonObject.peekInt(vararg keys: String): Int? {
        keys.forEach { key ->
            val primitive = this[key]?.jsonPrimitive ?: return@forEach
            val direct = primitive.intOrNull
            if (direct != null) return direct
            val doubleValue = primitive.doubleOrNull
            if (doubleValue != null) return doubleValue.toInt()
            val content = primitive.stringOrNull()
            val parsed = content?.toDoubleOrNull()
            if (parsed != null) return parsed.toInt()
        }
        return null
    }

    private fun JsonObject.peekArrayString(vararg keys: String): String? {
        keys.forEach { key ->
            val element = this[key]
            when (element) {
                is JsonArray -> element.firstStringOrNull()?.let { return it }
                is JsonPrimitive -> element.stringOrNull()?.let { return it }
                is JsonObject -> element.asStringOrNull()?.let { return it }
                else -> Unit
            }
        }
        return null
    }

    private fun JsonArray.firstStringOrNull(): String? =
        this.firstOrNull()?.jsonPrimitive?.stringOrNull()
}

private fun JsonElement?.asStringOrNull(): String? = when (this) {
    null -> null
    is JsonNull -> null
    is JsonPrimitive -> this.stringOrNull()
    else -> null
}

private fun JsonPrimitive.stringOrNull(): String? = when {
    this is JsonNull -> null
    content.equals("null", ignoreCase = true) -> null
    else -> content
}
