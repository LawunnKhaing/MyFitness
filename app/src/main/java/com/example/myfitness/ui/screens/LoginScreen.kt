package com.example.myfitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myfitness.R
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText // Import this
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle



@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(30.dp))
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFFFFFFF))
                .padding(top = 46.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.hey_there),
                color = Color(0xFF1D1517),
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(bottom = 14.dp)
            )
            Text(
                text = stringResource(id = R.string.welcome_back),
                color = Color(0xFF1D1517),
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(bottom = 33.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 15.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF7F8F8),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clip(shape = RoundedCornerShape(14.dp))
                    .fillMaxWidth()
                    .background(color = Color(0xFFF7F8F8))
                    .padding(17.dp)
            ) {
                AsyncImage(
                    model = "https://banner2.cleanpng.com/20180319/lyw/av0qxsb65.webp",
                    contentDescription = "Email Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .width(15.dp)
                        .height(13.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(id = R.string.email)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF7F8F8),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clip(shape = RoundedCornerShape(14.dp))
                    .fillMaxWidth()
                    .background(color = Color(0xFFF7F8F8))
                    .padding(vertical = 17.dp, horizontal = 19.dp)
            ) {
                AsyncImage(
                    model = "https://e7.pngegg.com/pngimages/786/101/png-clipart-password-computer-icons-security-safety-icon-safety-icon-child-safety-lock-thumbnail.png",
                    contentDescription = "Password Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .width(11.dp)
                        .height(13.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.password)) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }

            Text(
                text = stringResource(id = R.string.forgot_password),
                color = Color(0xFFACA3A5),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Brush.linearGradient(colors = listOf(Color(0xFFCC8FED), Color(0xFF6B50F6))))
            ) {
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("home")
                                    } else {
                                        errorMessage = "Username or password is incorrect."
                                    }
                                }
                        } else {
                            errorMessage = "Please fill in all fields."
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.login),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // Divider with "Or"
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 22.dp)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF1D1517)))
                Text(text = stringResource(id = R.string.or), color = Color(0xFF1D1517), fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF1D1517)))
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clickable(onClick = {
                        navController.navigate("signup") // Navigate to signup screen
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Don’t have an account yet? ")
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        append("Register")
                        pop()
                    },
                    style = TextStyle(fontSize = 14.sp),
                )
            }

        }
    }
}