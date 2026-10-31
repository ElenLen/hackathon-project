package com.example.smarthome.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinDarkGray
import com.example.smarthome.ui.theme.UjinLightGray
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.smarthome.R

@Composable
fun OtpScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    // Логика автозаполнения и перехода
    LaunchedEffect(Unit) {
        delay(1500) // Ждем 1.5 секунды
        code = "676767"
        
        delay(1500) // Ждем еще 1.5 секунды после появления кода
        onLoginSuccess() // Переходим на главный экран
    }

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

        // Поле ввода кода (разбитое на ячейки)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Отрисовываем 6 ячеек
            for (i in 0 until 6) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f) // Квадратные ячейки
                        .background(UjinLightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Если в строке code есть символ с индексом i, показываем его
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Получить новый код можно через ", fontSize = 12.sp, color = Color.Gray)
            Text("00:54", fontSize = 12.sp, color = UjinCyan)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка изменить номер
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