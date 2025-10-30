package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.seller.CreateBatteryRequest
import com.example.evsecondhand.data.model.seller.CreateVehicleRequest
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.SellerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class SellerCreateUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SellerCreateListingViewModel(
    application: Application,
    private val repository: SellerRepository
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: Application,
            accessToken: String
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SellerCreateListingViewModel::class.java)) {
                        val repository = SellerRepository(
                            RetrofitClient.sellerApi,
                            accessToken
                        )
                        return SellerCreateListingViewModel(application, repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(SellerCreateUiState())
    val uiState: StateFlow<SellerCreateUiState> = _uiState.asStateFlow()

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    fun createVehicle(request: CreateVehicleRequest, imageUris: List<Uri>) {
        submitListing {
            repository.createVehicle(request, buildImageParts(imageUris))
        }
    }

    fun createBattery(request: CreateBatteryRequest, imageUris: List<Uri>) {
        submitListing {
            repository.createBattery(request, buildImageParts(imageUris))
        }
    }

    private fun submitListing(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.value = SellerCreateUiState(isSubmitting = true)
            action()
                .onSuccess {
                    _uiState.value = SellerCreateUiState(successMessage = "Tạo tin đăng thành công!")
                }
                .onFailure { throwable ->
                    _uiState.value = SellerCreateUiState(
                        errorMessage = throwable.message ?: "Không thể tạo tin đăng, vui lòng thử lại."
                    )
                }
        }
    }

    private fun buildImageParts(uris: List<Uri>): List<MultipartBody.Part> {
        val resolver = getApplication<Application>().contentResolver
        val mediaType = "image/*".toMediaType()
        return uris.mapIndexed { index, uri ->
            val fileName = resolver.query(uri, null, null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
                }
                ?: "image_${System.currentTimeMillis()}_$index.jpg"

            val bytes = resolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            } ?: throw IllegalStateException("Unable to read selected image.")

            val requestBody = bytes.toRequestBody(mediaType)
            MultipartBody.Part.createFormData("images", fileName, requestBody)
        }
    }
}
