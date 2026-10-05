package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.NooshCharacterView
import com.example.presentation.theme.NooshPrimary
import com.example.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var registerNameInput by remember { mutableStateOf("") }
    var registerUsernameInput by remember { mutableStateOf("") }
    var registerEmailInput by remember { mutableStateOf("") }
    var registerPasswordInput by remember { mutableStateOf("") }
    var registerConfirmPasswordInput by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("register_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("btn_register_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            NooshCharacterView(size = 72.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ثبت‌نام در نوش",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "حساب کاربری جدید بسازید تا برنامه سلامت و آب روزانه شما تنظیم شود",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Error Banner
                    if (!errorMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 1. Full Name
                    OutlinedTextField(
                        value = registerNameInput,
                        onValueChange = {
                            registerNameInput = it
                            errorMessage = null
                        },
                        label = { Text("نام و نام خانوادگی") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_name"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Username (Unique)
                    OutlinedTextField(
                        value = registerUsernameInput,
                        onValueChange = {
                            registerUsernameInput = it.lowercase().trim()
                            errorMessage = null
                        },
                        label = { Text("نام کاربری (یکتا)") },
                        placeholder = { Text("مثال: noosh_user") },
                        prefix = { Text("@") },
                        supportingText = {
                            Text("تنها حروف انگلیسی، ارقام و خط زیر (یکتا در برنامه)", fontSize = 11.sp)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_username"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Email (Unique)
                    OutlinedTextField(
                        value = registerEmailInput,
                        onValueChange = {
                            registerEmailInput = it.trim()
                            errorMessage = null
                        },
                        label = { Text("آدرس ایمیل (یکتا)") },
                        placeholder = { Text("mail@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_email"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Password
                    OutlinedTextField(
                        value = registerPasswordInput,
                        onValueChange = {
                            registerPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("رمز عبور") },
                        placeholder = { Text("حداقل ۸ کاراکتر") },
                        visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            val image = if (registerPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val desc = if (registerPasswordVisible) "مخفی کردن رمز" else "نمایش رمز"
                            IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_password"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Confirm Password
                    OutlinedTextField(
                        value = registerConfirmPasswordInput,
                        onValueChange = {
                            registerConfirmPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("تکرار رمز عبور") },
                        visualTransformation = if (registerConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            val image = if (registerConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val desc = if (registerConfirmPasswordVisible) "مخفی کردن رمز" else "نمایش رمز"
                            IconButton(onClick = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_confirm_password"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Registration Button
                    Button(
                        onClick = {
                            val name = registerNameInput.trim()
                            val username = registerUsernameInput.trim().removePrefix("@")
                            val email = registerEmailInput.trim().lowercase()
                            val pwd = registerPasswordInput.trim()
                            val confirmPwd = registerConfirmPasswordInput.trim()

                            if (name.isBlank()) {
                                errorMessage = "لطفاً نام و نام خانوادگی خود را وارد کنید."
                                return@Button
                            }
                            if (username.isBlank()) {
                                errorMessage = "لطفاً یک شناسه کاربری (نام کاربری) وارد کنید."
                                return@Button
                            }
                            if (!Regex("^[a-zA-Z0-9_]{3,30}$").matches(username)) {
                                errorMessage = "نام کاربری باید بین ۳ تا ۳۰ کاراکتر و تنها شامل حروف انگلیسی، اعداد یا خط زیر باشد."
                                return@Button
                            }
                            if (email.isBlank()) {
                                errorMessage = "لطفاً آدرس ایمیل خود را وارد کنید."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "فرمت آدرس ایمیل وارد شده معتبر نیست."
                                return@Button
                            }
                            if (pwd.length < 8) {
                                errorMessage = "رمز عبور باید حداقل ۸ کاراکتر باشد."
                                return@Button
                            }
                            if (pwd != confirmPwd) {
                                errorMessage = "رمز عبور و تکرار آن با یکدیگر مطابقت ندارند."
                                return@Button
                            }

                            coroutineScope.launch {
                                // Async uniqueness validation before submitting
                                val isEmailFree = viewModel.checkEmailAvailable(email)
                                if (!isEmailFree) {
                                    errorMessage = "این ایمیل قبلاً در برنامه ثبت‌نام شده است. لطفاً وارد حساب خود شوید."
                                    return@launch
                                }

                                val isUsernameFree = viewModel.checkUsernameAvailable(username)
                                if (!isUsernameFree) {
                                    errorMessage = "این نام کاربری قبلاً انتخاب شده است. لطفاً شناسه دیگری برگزینید."
                                    return@launch
                                }

                                errorMessage = null
                                isSubmitting = true
                                viewModel.signUpUser(
                                    email = email,
                                    name = name,
                                    password = pwd,
                                    username = username
                                ) { isSuccess, message ->
                                    isSubmitting = false
                                    if (isSuccess) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        // Brand new user registration: route to Onboarding wizard!
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = message
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_register"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("ثبت‌نام و شروع آنبوردینگ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قبلاً ثبت‌نام کرده‌اید؟",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.testTag("btn_go_to_login")
                        ) {
                            Text("وارد حساب شوید", color = NooshPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
