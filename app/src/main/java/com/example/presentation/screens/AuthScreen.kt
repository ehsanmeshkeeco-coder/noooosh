package com.example.presentation.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.clerk.AuthState
import com.example.presentation.components.NooshCharacterView
import com.example.presentation.theme.NooshPrimary
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.viewmodel.MainViewModel

enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit = onNavigateBack,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by viewModel.authState.collectAsState()

    // When the user is NOT authenticated, pressing back exits the app
    BackHandler(enabled = authState !is AuthState.Authenticated) {
        activity?.finish()
    }

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Login Form State
    var loginIdentifierInput by remember { mutableStateOf("") }
    var loginPasswordInput by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Form State
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Button (Only visible if user is already authenticated and opened from Profile)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            if (authState is AuthState.Authenticated) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("btn_auth_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        NooshCharacterView(size = 80.dp)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (authMode == AuthMode.LOGIN) "ورود به حساب کاربری" else "ثبت‌نام در نوش",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (authMode == AuthMode.LOGIN)
                "برای همگام‌سازی و دسترسی به سوابق مصرف آب خود وارد شوید"
            else
                "اطلاعات خود را وارد کنید تا حساب کاربری شما ساخته و فعال شود",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        when (val state = authState) {
            is AuthState.Authenticated -> {
                // User is currently authenticated: display profile & account control
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = state.user.firstName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = state.user.email,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "حساب کاربری شما فعال و متصل است ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onAuthSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_enter_app"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("ورود به برنامه", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.logout {
                                    Toast.makeText(context, "از حساب کاربری خارج شدید", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_sign_out"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("خروج از حساب کاربری", color = Color(0xFFDC2626), fontSize = 13.sp)
                        }
                    }
                }
            }

            else -> {
                // User is unauthenticated: display separate Login / Register forms
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
                            .padding(20.dp)
                    ) {
                        // Top Switch Tabs: Login vs Register
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (authMode == AuthMode.LOGIN) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable {
                                        authMode = AuthMode.LOGIN
                                        errorMessage = null
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("tab_auth_login"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ورود به حساب",
                                    fontSize = 14.sp,
                                    fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                    color = if (authMode == AuthMode.LOGIN) NooshPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (authMode == AuthMode.REGISTER) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable {
                                        authMode = AuthMode.REGISTER
                                        errorMessage = null
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("tab_auth_register"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ثبت‌نام کاربر جدید",
                                    fontSize = 14.sp,
                                    fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                                    color = if (authMode == AuthMode.REGISTER) NooshPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

                        AnimatedContent(
                            targetState = authMode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "auth_form_anim"
                        ) { mode ->
                            when (mode) {
                                AuthMode.LOGIN -> {
                                    // LOGIN FORM
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = loginIdentifierInput,
                                            onValueChange = {
                                                loginIdentifierInput = it
                                                errorMessage = null
                                            },
                                            label = { Text("ایمیل یا شناسه کاربری") },
                                            placeholder = { Text("example@mail.com یا noosh_user") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_login_identifier"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            ),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = loginPasswordInput,
                                            onValueChange = {
                                                loginPasswordInput = it
                                                errorMessage = null
                                            },
                                            label = { Text("رمز عبور") },
                                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            trailingIcon = {
                                                val image = if (loginPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                                val desc = if (loginPasswordVisible) "مخفی کردن رمز" else "نمایش رمز"
                                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                                    Icon(imageVector = image, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_login_password"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            ),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                val identifier = loginIdentifierInput.trim()
                                                val pwd = loginPasswordInput.trim()

                                                if (identifier.isBlank()) {
                                                    errorMessage = "لطفاً ایمیل یا شناسه کاربری خود را وارد کنید."
                                                    return@Button
                                                }
                                                if (pwd.isBlank()) {
                                                    errorMessage = "لطفاً رمز عبور را وارد کنید."
                                                    return@Button
                                                }

                                                errorMessage = null
                                                isSubmitting = true
                                                viewModel.signInUser(identifier, pwd) { isSuccess, message ->
                                                    isSubmitting = false
                                                    if (isSuccess) {
                                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                        onAuthSuccess()
                                                    } else {
                                                        errorMessage = message
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            enabled = !isSubmitting,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("btn_submit_login"),
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
                                                Text("ورود به حساب کاربری", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "هنوز حساب کاربری ندارید؟",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            TextButton(
                                                onClick = {
                                                    authMode = AuthMode.REGISTER
                                                    errorMessage = null
                                                }
                                            ) {
                                                Text("ثبت‌نام کنید", color = NooshPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.continueAsGuest()
                                                onAuthSuccess()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("btn_auth_guest"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("ادامه به عنوان کاربر مهمان", fontSize = 13.sp)
                                        }
                                    }
                                }

                                AuthMode.REGISTER -> {
                                    // REGISTER FORM
                                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                                Text("شامل حروف انگلیسی، ارقام و خط زیر (یکتا)", fontSize = 11.sp)
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

                                        Button(
                                            onClick = {
                                                val name = registerNameInput.trim()
                                                val username = registerUsernameInput.trim().removePrefix("@")
                                                val email = registerEmailInput.trim()
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
                                                        onAuthSuccess()
                                                    } else {
                                                        errorMessage = message
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
                                                Text("ثبت‌نام و ایجاد حساب", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "قبلاً حساب کاربری ساخته‌اید؟",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            TextButton(
                                                onClick = {
                                                    authMode = AuthMode.LOGIN
                                                    errorMessage = null
                                                }
                                            ) {
                                                Text("اینجا وارد شوید", color = NooshPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
