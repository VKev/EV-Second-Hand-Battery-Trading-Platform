package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.AuctionDetailResponse
import com.example.evsecondhand.data.model.AuctionLiveResponse
import com.example.evsecondhand.data.model.AuctionStatusResponse
import com.example.evsecondhand.data.model.BidRequest
import com.example.evsecondhand.data.model.AuctionDepositResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuctionApiService {

    @GET("auctions/{listingType}/{listingId}")
    suspend fun getAuctionStatus(
        @Path("listingType") listingType: String,
        @Path("listingId") listingId: String
    ): AuctionStatusResponse

    @GET("auctions/{listingType}/{listingId}")
    suspend fun getAuctionDetail(
        @Path("listingType") listingType: String,
        @Path("listingId") listingId: String
    ): AuctionDetailResponse

    @GET("auctions/live")
    suspend fun getLiveAuctions(
        @Query("time") time: String
    ): AuctionLiveResponse

    @POST("auctions/{listingType}/{listingId}/deposit")
    suspend fun placeDeposit(
        @Path("listingType") listingType: String,
        @Path("listingId") listingId: String
    ): AuctionDepositResponse

    @POST("auctions/{listingType}/{listingId}/bids")
    suspend fun placeBid(
        @Path("listingType") listingType: String,
        @Path("listingId") listingId: String,
        @Body request: BidRequest
    ): AuctionDetailResponse
}
