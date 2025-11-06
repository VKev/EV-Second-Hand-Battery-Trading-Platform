package com.example.evsecondhand.ui.screen.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.PrimaryGreenDark
import com.example.evsecondhand.ui.viewmodel.AuthState
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            Toast.makeText(context, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                authViewModel.onGoogleIdToken(idToken)
            } else {
                Toast.makeText(context, "Missing Google ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (exception: ApiException) {
            Toast.makeText(
                context,
                "Google sign-in failed: ${exception.statusCode}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (exception: Exception) {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryGreen.copy(alpha = 0.05f),
                        Color(0xFFF8FFF8),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Section with Animation Effect
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGreen
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "EV Market",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Text(
                text = "EV Market",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thị trường pin & xe điện cũ",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Chào mừng bạn quay trở lại!",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", fontSize = 14.sp) },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Email, 
                                contentDescription = "Email",
                                tint = PrimaryGreen
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            cursorColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu", fontSize = 14.sp) },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = "Password",
                                tint = PrimaryGreen
                            ) 
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password"
                                    else "Show password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            cursorColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (authState is AuthState.Error) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Login Button
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = authState !is AuthState.Loading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Đăng nhập", 
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "HOẶC",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Google Button
                    OutlinedButton(
                        onClick = {
                            val googleAuthUrl = RetrofitClient.BASE_URL + "auth/google?client_type=mobile"
                            uriHandler.openUri(googleAuthUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = authState !is AuthState.Loading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, 
                            Color.LightGray.copy(alpha = 0.4f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Card(
                                    modifier = Modifier.size(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Đăng nhập với Google", 
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Chưa có tài khoản? ", 
                            color = Color.Gray, 
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Đăng ký", 
                                color = PrimaryGreen, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

