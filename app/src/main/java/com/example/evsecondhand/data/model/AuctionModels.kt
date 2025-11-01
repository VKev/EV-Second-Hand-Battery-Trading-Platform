package com.example.evsecondhand.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

@Serializable
data class AuctionStatusResponse(
    val message: String? = null,
    val data: AuctionStatusData? = null
)

@Serializable
data class AuctionStatusData(
    @SerialName("isBidder")
    val isBidder: Boolean? = null,
    @SerialName("hasDeposit")
    val hasDeposit: Boolean? = null
)

@Serializable
data class AuctionDepositResponse(
    val message: String? = null,
    val data: AuctionStatusData? = null
)

@Serializable
data class AuctionLiveResponse(
    val message: String? = null,
    val data: JsonElement? = JsonNull
)

data class AuctionSummary(
    val id: String? = null,
    val listingId: String,
    val listingType: String,
    val title: String?,
    val imageUrl: String?,
    val startingPrice: Int?,
    val currentBid: Int?,
    val depositAmount: Int?,
    val auctionStartsAt: String?,
    val auctionEndsAt: String?
)
@Serializable
data class AuctionDetailResponse(
    val message: String? = null,
    val data: AuctionDetailData? = null
)

@Serializable
data class AuctionDetailData(
    @SerialName("id")
    val id: String? = null,
    @SerialName("listingId")
    val listingId: String? = null,
    @SerialName("listingType")
    val listingType: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("images")
    val images: List<String>? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("startingPrice")
    val startingPrice: Int? = null,
    @SerialName("currentBid")
    val currentBid: Int? = null,
    @SerialName("bidIncrement")
    val bidIncrement: Int? = null,
    @SerialName("depositAmount")
    val depositAmount: Int? = null,
    @SerialName("hasUserDeposit")
    val hasUserDeposit: Boolean? = null,
    @SerialName("hasDeposit")
    val hasDeposit: Boolean? = null,
    @SerialName("hasUserBid")
    val hasUserBid: Boolean? = null,
    @SerialName("metadata")
    val metadata: JsonElement? = null,
    @SerialName("listing")
    val listing: JsonElement? = null,
    @SerialName("auctionStartsAt")
    val auctionStartsAt: String? = null,
    @SerialName("auctionEndsAt")
    val auctionEndsAt: String? = null,
    @SerialName("status")
    val status: String? = null
)

@Serializable
data class BidRequest(
    val amount: Int
)
