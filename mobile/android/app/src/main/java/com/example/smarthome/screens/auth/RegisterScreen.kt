package com.example.smarthome.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.UjinDarkGray
import com.example.smarthome.ui.theme.UjinLightGray
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.smarthome.R
import androidx.compose.ui.draw.scale

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onCodeSent: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = UjinDarkGray
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_ujin),
            contentDescription = "Ujin Logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(180.dp)
                .wrapContentHeight()
                .scale(2f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Создать аккаунт",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Имя", fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(UjinLightGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                decorationBox = { innerTextField ->
                    if (name.isEmpty()) {
                        Text("Введите имя", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Телефон", fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(UjinLightGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                decorationBox = { innerTextField ->
                    if (phoneNumber.isEmpty()) {
                        Text("Введите номер телефона", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = isAgreed,
                onCheckedChange = { isAgreed = it },
                colors = CheckboxDefaults.colors(checkedColor = UjinDarkGray)
            )
            Text(
                text = "Согласен на обработку персональных данных и получение информационных сообщений",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCodeSent,
            enabled = isAgreed && name.isNotBlank() && phoneNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UjinDarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Получить код", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = UjinDarkGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Назад", color = UjinDarkGray)
        }
    }
}