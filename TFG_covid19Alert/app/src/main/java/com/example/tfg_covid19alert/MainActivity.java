package com.example.tfg_covid19alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    //variables globales
    private static final String TAG = "taglog";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener maAthlist;
    private EditText user, contra;
    private Button acceso, registro;


    /**
     * Metodo on create donde se asignan funciones a los botones disponibles en ese layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se obtienen los datos de lo que haya escrito el usuario
        user = (EditText) findViewById(R.id.eT_dni);
        contra = (EditText)findViewById(R.id.eT_contra);
        acceso =(Button)findViewById(R.id.accesoU);
        registro =(Button)findViewById(R.id.registroU);

        //Se inicial mAuth
        mAuth = FirebaseAuth.getInstance();

        //Metodo inicializado cuando el estatus de autenticacion cambia
        maAthlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Si el usuario existe, va al otro activity
                if(firebaseAuth.getCurrentUser()!=null){
                    Intent i = new Intent(MainActivity.this, Resumen.class);
                    i.putExtra("dni", user.getText().toString());
                    startActivity(i);
                }
            }};

        //si se clica en el boton del registro se iniciara su propia activity (Registro.class)
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Registro.class);
                startActivity(i);
            }
        });

        //boton para el acceso del usuario que se haya escrito en los espacios anteriores
        acceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccesoUsuario();
            }
        });
    }

    /**
     * Si el usuario esta loggeado accede directamente a la segunda pantalla
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(maAthlist);
    }

    /**
     * Método para el acceso de usuarios ya registrados
     * Información sacada del manual de Firebase implícito en Android Studio
     */
    public void AccesoUsuario(){
            String dniU, contraU;
            boolean lleno = texto();
            //Comprueba que el texto no esté vacío
            if(lleno){
                dniU = user.getText().toString()+"@gmail.com";
                contraU = contra.getText().toString();
                mAuth.signInWithEmailAndPassword(dniU, contraU)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //El usuario ha podido acceder
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //El usuario no ha podido acceder
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, "Este usuario no existe",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }

    /**
     * Comprueba que el texto del usuario y contraseña esten completos
     * En caso de que no sea asi, se mostrara un toast conforme se obliga al usuario a ingresarlo
     * @return devuelve si los datos estan completos o no
     */
    public boolean texto(){
            if(!(TextUtils.isEmpty(user.getText().toString())||TextUtils.isEmpty(contra.getText().toString()))){
                return true;
            }else{
                Toast.makeText(MainActivity.this, "Ingrese un usuario y una contraseña",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

}
