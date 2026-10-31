package com.example.smarthome.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinDarkGray
import com.example.smarthome.ui.theme.UjinLightGray
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.smarthome.R

private const val DEMO_CODE = "676767"

@Composable
fun OtpScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            text = "Введите код",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box {
            BasicTextField(
                value = code,
                onValueChange = { input ->
                    code = input.filter { it.isDigit() }.take(6)
                    if (error) error = false
                },
                textStyle = TextStyle(fontSize = 1.sp, color = Color.Transparent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                modifier = Modifier.matchParentSize()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(UjinLightGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (code.length > i) {
                            Text(
                                text = code[i].toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Запросить код можно через ", fontSize = 12.sp, color = Color.Gray)
            Text("00:54", fontSize = 12.sp, color = UjinCyan)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (error) {
            Text(
                text = "Неверный код. Демо-код: 676767",
                fontSize = 12.sp,
                color = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (code == DEMO_CODE) {
                    error = false
                    onLoginSuccess()
                } else {
                    error = true
                }
            },
            enabled = code.length == 6,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UjinDarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Войти", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UjinDarkGray)
        ) {
            Text("Изменить номер")
        }
    }
}