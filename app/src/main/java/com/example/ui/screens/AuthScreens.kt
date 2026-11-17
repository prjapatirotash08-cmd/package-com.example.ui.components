package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateAdminLogin: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AuthScaffold(
        title = "Welcome Back",
        subtitle = "Sign in to access your AI Photo Studio workspace & projects.",
        onBack = onBack
    ) {
        LuxuryCard(highlighted = true) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryGold) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            GoldGradientButton(
                text = "Sign In",
                onClick = { onLogin(email, password) },
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_submit_button")
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateForgotPassword) {
                    Text("Forgot Password?", color = SecondaryGold)
                }
                TextButton(onClick = onNavigateAdminLogin) {
                    Text("Admin Portal", color = TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        GoldOutlinedButton(
            text = "Create New Account",
            onClick = onNavigateRegister,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_to_register_button")
        )
    }
}

@Composable
fun RegisterScreen(
    starterCredits: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (name: String, email: String, password: String, referralCode: String) -> Unit,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    AuthScaffold(
        title = "Create Studio Account",
        subtitle = "Start with $starterCredits free studio credits. Saved securely with salted SHA-256.",
        onBack = onBack
    ) {
        LuxuryCard(highlighted = true) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6 characters)") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryGold) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = referralCode,
                onValueChange = { referralCode = it },
                label = { Text("Referral Code (Optional, +10 credits)") },
                leadingIcon = { Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            GoldGradientButton(
                text = "Create Account & Claim $starterCredits Credits",
                onClick = { onRegister(name, email, password, referralCode) },
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_submit_button")
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Already have an account? Sign In", color = SecondaryGold)
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onResetPassword: (email: String, newPassword: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AuthScaffold(
        title = "Reset Password",
        subtitle = "Update the password for your registered studio email.",
        onBack = onBack
    ) {
        LuxuryCard(highlighted = true) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Registered Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password (min 6 characters)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            GoldGradientButton(
                text = "Update Password",
                onClick = { onResetPassword(email, newPassword) },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AdminLoginScreen(
    hasAdminAccount: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onAdminLogin: (email: String, password: String) -> Unit,
    onCreateInitialAdmin: (name: String, email: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("Studio Admin") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScaffold(
        title = if (hasAdminAccount) "Admin Portal Sign In" else "Initialize Admin Account",
        subtitle = if (hasAdminAccount) {
            "Role-verified administrator access for managing users, tools, credits, plans, and analytics."
        } else {
            "No administrator exists yet. Create the primary Admin account to unlock the Admin Panel."
        },
        onBack = onBack
    ) {
        LuxuryCard(highlighted = true) {
            if (!hasAdminAccount) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Admin Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Admin Email") },
                leadingIcon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_email_input")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Admin Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryGold) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_password_input")
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            GoldGradientButton(
                text = if (hasAdminAccount) "Sign In to Admin Panel" else "Create Admin & Open Panel",
                onClick = {
                    if (hasAdminAccount) onAdminLogin(email, password)
                    else onCreateInitialAdmin(name, email, password)
                },
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_submit_button")
            )
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.displayMedium, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(24.dp))
        content()
    }
}
