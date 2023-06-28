package com.example.tfg_covid19alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registro extends AppCompatActivity {

    //variables globales
    private static final String TAG = "taglog";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener maAthlist;
    DatabaseReference mydb = FirebaseDatabase.getInstance().getReference();
    EditText dni, contra, nombre;
    private Spinner comunid;
    String Scomunidad="";
    Button aceptar, cancelar;

    /**
     * Metodo on create donde se asignan funciones a los botones disponibles en el layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Se obtienen los datos de lo que haya escrito el usuario
        comunid = (Spinner)findViewById(R.id.spinner2);
        dni = (EditText)findViewById(R.id.eT_dni);
        nombre = (EditText)findViewById(R.id.eT_nombre);
        contra = (EditText)findViewById(R.id.eT_contra);
        aceptar =(Button)findViewById(R.id.buttonAR);
        cancelar =(Button)findViewById(R.id.buttonCancelar);
        //obtencion de la comunidad que haya elegido el usuario
        comunid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Scomunidad=comunid.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        maAthlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Si el usuario existe, va al otro activity
                if(firebaseAuth.getCurrentUser()!=null){
                    Intent i = new Intent(Registro.this, Resumen.class);
                    startActivity(i);
                }
            }};

        //cuando se pulsa en aceptar se crea un nuevo usuario
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NuevoUsuario();
            }
        });

        //si se pulsa en cancelar se vuelve a la pantalla de incio de sesion
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Registro.this, MainActivity.class);
                i.putExtra("dni", dni.getText().toString());
                startActivity(i);
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
     * Información sacada del manual de Firebase implícito en Android Studio
     */
    public void NuevoUsuario(){
        String dniU, contraU="";
        boolean lleno = texto();
        boolean dnivalido = compruebaParametrosDNI();
        //Comprueba que el texto no esté vacío
        if(lleno){
            if(dnivalido) {
                dniU = dni.getText().toString() + "@gmail.com";
                contraU = contra.getText().toString();
                if(!(contraU.length() <6)) {
                    mAuth.createUserWithEmailAndPassword(dniU, contraU)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //El usuario ha podido acceder
                                    if (task.isSuccessful()) {
                                        //se llama al siguiente metodo para grabar correctamente los datos en la bd
                                        databaseUsuarios();
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        //El usuario no ha podido acceder
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Registro.this, "Ya hay un usuario asociado",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(Registro.this, "El número mínimo de caracteres es de 6",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Comprueba que el texto del usuario y contraseña esten completos
     * En caso de que no sea asi, se mostrara un toast conforme se obliga al usuario a ingresarlo
     * @return devuelve si los datos estan completos o no
     */
    public boolean texto(){
        if(!(TextUtils.isEmpty(dni.getText().toString())||TextUtils.isEmpty(contra.getText().toString())||TextUtils.isEmpty(nombre.getText().toString()))){
            return true;
        }else{
            Toast.makeText(Registro.this, "Rellene todos los campos",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * comprueba que el dni sea correcto
     * https://www.ruidoquattro.com/blog/entry?code=7XB1M
     * @return correcto=true (es correcto) ; correcto = false (dni no correcto)
     */
    public boolean compruebaParametrosDNI(){
        boolean correcto = false;
        Pattern pattern = Pattern.compile("(\\d{1,8})([TRWAGMYFPDXBNJZSQVHLCKEtrwagmyfpdxbnjzsqvhlcke])");
        Matcher matcher = pattern.matcher(dni.getText().toString());
        if (matcher.matches()) {
            String letra = matcher.group(2);
            String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
            int index = Integer.parseInt(matcher.group(1));
            index = index % 23;
            String reference = letras.substring(index, index + 1);
            if (reference.equalsIgnoreCase(letra)) {
                correcto = true;
            } else {
                correcto = false;
            }
        } else {
            correcto = false;
        }
        if(!correcto){
            Toast.makeText(Registro.this, "Introduzca un DNI válido",
                    Toast.LENGTH_SHORT).show();
        }
        return correcto;
    }

    /**
     * agrega datos a usuarios
     */
    public void databaseUsuarios(){
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("dni", dni.getText().toString());
        datosUsuario.put("nombre", nombre.getText().toString());
        datosUsuario.put("passw", contra.getText().toString());
        datosUsuario.put("activo", false);
        datosUsuario.put("contag", false);
        datosUsuario.put("cuarent", false);
        datosUsuario.put("crono", "STOP/_/_/_"); //establecera el tiempo de cuarentena

        //agrega los datos introducidos en la "tabla" usuarios de la bd
        mydb.child("usuarios").push().setValue(datosUsuario);
        databaseComXUsers();
    }

    public HashMap<String, String> getComunidad() {
        HashMap<String, String> comunidades = new HashMap<String, String>() {{
            put("Andalucia", "1");
            put("Aragon", "2");
            put("Asturias", "3");
            put("Cantabria", "4");
            put("C La Mancha", "5");
            put("C Y Leon", "6");
            put("Canarias", "7");
            put("Cataluña", "8");
            put("Ceuta", "9");
            put("C Madrid", "10");
            put("C Valenciana", "11");
            put("Extremadura", "12");
            put("Galicia", "13");
            put("I Baleares", "14");
            put("La Rioja", "15");
            put("Melilla", "16");
            put("Murcia", "17");
            put("Navarra", "18");
            put("Pais Vasco", "19");
        }};
        
        return comunidades;
    }

    /**
    * Relaciona los usuarios con su comunidad autónoma y hace un listado
    */
    public void databaseComXUsers() {
        HashMap<String, String> comunidades = getComunidad();
        String idCom = comunidades.getOrDefault(Scomunidad, "null");

        // El número se asignará a la ruta de la base de datos como referencia
        DatabaseReference myChild = mydb.child("comxusers/" + idCom);
        // Y se agregará la relación entre la comunidad y DNI a través de una clave única (push)
        myChild.push().setValue(dni.getText().toString());
    }

}