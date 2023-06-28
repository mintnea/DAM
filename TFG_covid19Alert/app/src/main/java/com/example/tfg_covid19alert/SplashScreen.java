package com.example.tfg_covid19alert;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Clase con función decorativa
 * Se muestra el layout de activity_splash_screen durante 4000 milisegundos
 * Después ejecuta el MainActivity.class
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { //ejecuta la siguiente actividad

                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        }, 4000); //pero utiliza un delay para dar sensación de carga y poder ver el logo de la aplicacion
    }
}
