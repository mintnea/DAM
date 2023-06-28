package com.example.tfg_covid19alert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.tfg_covid19alert.pojo.UsersPojo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tfg_covid19alert.ui.resumen.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Resumen extends FragmentActivity implements FragmentLista.OnFragmentInteractionListener, FragmentMensajes.OnFragmentInteractionListener, ZXingScannerView.ResultHandler{

    //variables globales
    private TextView nombreBienvenida;
    private ZXingScannerView escanerview;
    private static final int codcamara=1;
    private Button deslog;
    boolean b=false;
    DatabaseReference mydb = FirebaseDatabase.getInstance().getReference();
    String emaildni = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String dni = emaildni.substring(0, 9); //Se quita email

    /**
     * Metodo on create donde se asignan funciones a los botones disponibles en ese layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);
        nombreBienvenida = findViewById(R.id.textView2);
        nombreUsuario();

        //b vale false porque aun no ha escaneado nada
        b=false;
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab);
        deslog= (Button)findViewById(R.id.buttonSalir);

        //Si pulsa el boton de salir se sale de su usuario en la aplicacion
        deslog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                //se vuelve al mainactivity
                startActivity(new Intent(Resumen.this, MainActivity.class));
            }
        });

        //Al pulsar aqui, empieza el proceso de escaneo de códigos QR
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //comprueba el estado del permiso
                int estadoPermiso= ContextCompat.checkSelfPermission(Resumen.this, Manifest.permission.CAMERA);
                if(!(estadoPermiso== PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(Resumen.this, new String[]{Manifest.permission.CAMERA}, codcamara);
                //cuando se obtenga ese permiso se inicia el servicio
                }else{
                    escanerview = new ZXingScannerView(Resumen.this);
                    setContentView(escanerview);
                    escanerview.setResultHandler(Resumen.this);
                    escanerview.startCamera();
                }
            }
        });
    }

    public void nombreUsuario(){
        DatabaseReference nombre = FirebaseDatabase.getInstance().getReference().child("usuarios");
        nombre.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                nombre.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue().toString().contains(dni.toUpperCase())) {
                            UsersPojo user = dataSnapshot.getValue(UsersPojo.class);
                            nombreBienvenida.setText("Bienvenid@ "+user.getNombre());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { } }); }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    /**
     * Metodo que gestiona el recibimiento de datos desde el codigo qr
     * @param result
     */
    @Override
    public void handleResult(Result result) {
        Log.v("Handle result", result.getText());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //primero comprueba que el codigo contenga la palabra clave (COVID) y que no se haya escaneado anteriormente el mismo codigo
        if(result.getText().contains("COVID")&&b==false){
            builder.setTitle("Resultado de escaneo");
            //el resultado del escaneo pasa a a ser true para que no vuelva a escanear una segunda vez
            b=true;

            //Se obtienen las partes del codigo QR
            String[] parts = result.getText().split("-");
            //el numero de local se representa con el segundo caracter de la cadena recibida por el codigo QR
            String local = parts[1];

            //se obtiene el usuario
            String emaildni = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String dni = emaildni.substring(0, 9);

            //se obtiene la fecha actual
            Calendar c1 = Calendar.getInstance();
            String fecha = (Integer.toString(c1.get(Calendar.DATE))) + "-" + (Integer.toString(c1.get(Calendar.MONTH)));

            //y se agrega a la base de datos
            DatabaseReference mychild = mydb.child("locxusers/"+fecha);
            mychild.child(local).push().setValue(dni);

            //despùes se vuelve al activity de resumen
            startActivity(new Intent(this, Resumen.class));
            this.finish();
        }
        escanerview.resumeCameraPreview(this);
    }

}