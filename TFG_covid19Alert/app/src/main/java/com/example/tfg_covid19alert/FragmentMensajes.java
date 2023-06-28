package com.example.tfg_covid19alert;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg_covid19alert.pojo.TelfCatchPojo;
import com.example.tfg_covid19alert.pojo.TelfPojo;
import com.example.tfg_covid19alert.pojo.UsersPojo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;


public class FragmentMensajes extends Fragment {
    //variables globales
    private TextView infoEstado;
    private String keyencontrada="", texto="", numtelf="", numtelf2="", txt="";
    private String telf="";
    private boolean contag=false, cuarent=false;
    private static int codllamada=1, diaenEsemomento=0;
    ImageView foto;

    private OnFragmentInteractionListener mListener;
    DatabaseReference mydb = FirebaseDatabase.getInstance().getReference();

    Calendar c1 = Calendar.getInstance();
    String fecha = (Integer.toString(c1.get(Calendar.DATE))) + "-" + (Integer.toString(c1.get(Calendar.MONTH)));

    String emaildni = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String dni = emaildni.substring(0, 9); //Se quita email

    private Button lanzarDialog;

    /**Método constructor
     */
    public FragmentMensajes() { }

    /**
     * Metodo on create del fragment
     * @param savedInstanceState
     */
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Metodo para la creación visual del fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_mensajes, container, false);
        foto = view.findViewById(R.id.estadoSaludImg);

        contagios();
        numCom();
        estadoSalud();


        infoEstado = view.findViewById(R.id.textEstadoSalud);
        infoEstado.setText(txt);

        lanzarDialog=view.findViewById(R.id.buttonEmergencia);
        lanzarDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    final AlertDialog.Builder alertD = new AlertDialog.Builder(getActivity());
                    alertD.setMessage("En qué te puedo ayudar").setCancelable(true)
                            .setPositiveButton(texto, new DialogInterface.OnClickListener() {
                                @Override

                                public void onClick(DialogInterface dialog, int which) {
                                    final AlertDialog.Builder alertSeguro = new AlertDialog.Builder(getActivity());
                                    alertSeguro.setMessage("Estás seguro?").setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //actualiza los datos del usuario y lo pone como contagiado
                                            DatabaseReference mychild = mydb.child("usuarios/" + keyencontrada);
                                            //si el usaurio ya no esta contagiado
                                            if(contag){
                                                mychild.child("contag").setValue(false);
                                                //mostrar mensaje
                                                if(cuarent){
                                                    final AlertDialog.Builder alertD = new AlertDialog.Builder(getActivity());
                                                    alertD.setMessage("Recuerda seguir en cuarentena el tiempo establecido por precaución").setCancelable(true).show();
                                                }
                                            //si el usuario notifica su estado de contagio se establecen parametros a true y se indica un crono inicial
                                            }else{
                                                mychild.child("contag").setValue(true);
                                                mychild.child("cuarent").setValue(true);
                                                mychild.child("crono").setValue("C/15/"+fecha+"/"+fecha);
                                                notificarContagio();
                                            }
                                            //despues se pone un aviso con forme
                                            final AlertDialog.Builder alertOk = new AlertDialog.Builder(getActivity());
                                            alertOk.setMessage("Gracias por tu aviso!")
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            alertOk.show();
                                            dialog.cancel();
                                        }
                                    })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    alertSeguro.show();
                                    dialog.cancel();
                                }
                            })
                            //mostrar la informacion telefonica segun la comunidad autonoma
                            .setNegativeButton("Información telefónica", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final AlertDialog.Builder alertTelf = new AlertDialog.Builder(getActivity());
                                    alertTelf.setMessage("El número de la COVID en tu comunidad es "+telf)
                                            .setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //pedi permisos y llamar
                                                    int estadoPermiso= ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);
                                                    if(!(estadoPermiso== PackageManager.PERMISSION_GRANTED)){
                                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, codllamada);
                                                    }else{
                                                        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:661844786"));
                                                        startActivity(i);
                                                    }
                                                    dialog.cancel();
                                                }
                                            })
                                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    dialog.cancel();
                                    alertTelf.show();
                                    dialog.cancel();
                                }
                            });

                    alertD.show();
            }
        });

        return view;
    }

    /**
     * Método para determinar el estado de contagio del usuario
     */
    public void contagios(){
        mydb.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //buscando en todos los datos
                for (final DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    UsersPojo usersPojo = snapshot1.getValue(UsersPojo.class);
                    String nombreobtenido = usersPojo.getDni();
                    //si se obtiene un dato dni igual al del auth, se guarda su key
                    if (nombreobtenido.equals(dni.toUpperCase())) {
                        keyencontrada = snapshot1.getKey();
                        contag = usersPojo.isContag();
                        cuarent = usersPojo.isCuarent();
                        if (contag) {
                            texto = "Ya no soy positivo en COVID";
                        } else {
                            texto = "Soy positivo en COVID";
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }});
    }

    /**
     * Método para buscar el numero de telefono
     */
    public void numCom() {
        //Se busca su codigo en la bd dentro de usuarios
        mydb.child("comxusers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //buscando en todos los datos
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //Segun las keys
                    String datoobtenido = snapshot.getValue().toString();
                    if(datoobtenido.contains(dni.toUpperCase())){
                        numtelf = snapshot.getKey();
                        mydb.child("comunidad").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                                    if(snap.getKey().equals(numtelf)){
                                        mydb.child("comunidad").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String recogerTelf = snap.getValue().toString();
                                                try{
                                                    TelfPojo tf = snap.getValue(TelfPojo.class);
                                                    telf = Integer.toString(tf.getTelf());
                                                }catch (Exception e){
                                                    TelfCatchPojo tf = snap.getValue(TelfCatchPojo.class);
                                                    telf =   tf.getTelf();
                                                }

                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        }); } }}
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }});
    }

    /** Método para modificar el crono de las personas que han estado en contacto con el afectado 15 dias
     */
    public void notificarContagio(){
            mydb.child("locxusers").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Se calculan 15 dias atras desde la fecha en la que se ha
                    ArrayList<String> longDiasC = new ArrayList<String>();
                    int diaTopeInt=0;
                    String diaTope="";
                    int diaTopeBeta = c1.get(Calendar.DATE) - 15;
                    int mesTopeBeta= c1.get(Calendar.MONTH);

                    if(diaTopeBeta<1){
                        diaTopeInt = diaTopeBeta + 30;
                        diaTope = Integer.toString(diaTopeInt);
                    }else{
                        diaTope = Integer.toString(diaTopeBeta);
                    }

                    for(int i=c1.get(Calendar.DATE); i != (Integer.valueOf(diaTope)); i--){
                        if(i<1){
                            mesTopeBeta=mesTopeBeta-1;
                            if(mesTopeBeta<1){
                                mesTopeBeta=12;
                            }
                            i=30;
                        }
                        longDiasC.add(Integer.toString(i)+"-"+Integer.toString(mesTopeBeta));
                    }

                    //buscando en cada dia hacia atras desde el actual
                    for (int i=0;i<longDiasC.size();i++) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(longDiasC.get(i))) {
                            //busca los usuarios dentro de ese dia
                            mydb.child("locxusers").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot snap : dataSnapshot.getChildren()) {
                                        //si en ese dia y ese lugar ha estado el infectado
                                        if (snap.getValue().toString().contains(dni)) {
                                            for (final DataSnapshot snap1 : snap.getChildren()) {
                                                //por cada usuario se establece un crono de contagio
                                                mydb.child("usuarios").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (final DataSnapshot snap2 : dataSnapshot.getChildren()) {
                                                            UsersPojo usuarioCoinc = snap2.getValue(UsersPojo.class);
                                                            if ((usuarioCoinc.getDni().equals(snap1.getValue().toString().toUpperCase())) && !(usuarioCoinc.getDni().equals(dni.toUpperCase()))) {
                                                                DatabaseReference mychild = mydb.child("usuarios/" + snap2.getKey());
                                                                mychild.child("crono").setValue("NC/15/" + fecha+"/_");
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) { }}); } } } }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }}); } }}}
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }}); }

    /**
     * Busca el estado de salud del usuario*/
    public void estadoSalud(){
            DatabaseReference estadoS = FirebaseDatabase.getInstance().getReference().child("usuarios");
            estadoS.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    estadoS.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue().toString().contains(dni.toUpperCase())){
                                UsersPojo user = dataSnapshot.getValue(UsersPojo.class);
                                String crono = user.getCrono();

                                String[] parts = crono.split("/");
                                String nuevoCaso = parts[0];
                                String diasRest = parts[1];
                                String fechaInicio = parts[2];
                                String ultimamodif = parts[3];

                                //si la primera parts no es STOP
                                if(!nuevoCaso.equals("STOP")){
                                    if(!ultimamodif.equals(fecha)) {
                                        String[] separarFecha = fechaInicio.split("-");
                                        String diaIni = separarFecha[0];
                                        String mesIni = separarFecha[1];

                                        //se calculan los dias restantes y el dia final
                                        int diasRestantes = Integer.valueOf(diasRest);
                                        int diaFinalBeta = Integer.valueOf(diaIni) + 15;
                                        int mesFinalBeta = Integer.valueOf(mesIni);
                                        String fechaFinal = "";

                                        if (diaFinalBeta > 30) {
                                            diaFinalBeta = diaFinalBeta - 30;
                                            mesFinalBeta = mesFinalBeta + 1;
                                            if (mesFinalBeta > 12) {
                                                mesFinalBeta = 1;
                                            }
                                        }
                                        fechaFinal = diaFinalBeta + "-" + mesFinalBeta;
                                        Log.e("fechafinal", fechaFinal);

                                        //si es un nuevo caso
                                        if (nuevoCaso.equals("NC")) {
                                            final AlertDialog.Builder alertD = new AlertDialog.Builder(getActivity());
                                            alertD.setMessage("Parece que ha entrado en contacto con gente infectada, manténgase en cuarentena 15 dias").setCancelable(true).show();
                                            DatabaseReference mychild = mydb.child("usuarios/" + keyencontrada);
                                            mychild.child("crono").setValue("C/" + (diasRestantes) + "/" + fechaInicio + "/" + fecha);
                                        }

                                        //si la fecha es igual a la fecha de finalizacion de la cuarentena
                                        if (fechaFinal.equals(fecha)) {
                                            DatabaseReference mychild = mydb.child("usuarios/" + keyencontrada);
                                            final AlertDialog.Builder alertD = new AlertDialog.Builder(getActivity());
                                            alertD.setMessage("Han pasado los 15 días de cuarentena, si todavía presenta síntomas, porfavor, notifíquelo").setCancelable(true).show();
                                            mychild.child("crono").setValue("STOP/_/_/_");
                                            mychild.child("contag").setValue(false);
                                            mychild.child("cuarent").setValue(false);

                                        //se resta un dia al crono si no se ha modificado
                                        } else if(!ultimamodif.equals(fecha)){
                                            txt="Quedan "+(diasRestantes-1)+" días de cuarentena";
                                            infoEstado.setText(txt);
                                            foto.setImageResource(R.drawable.emojicuarentena);
                                            DatabaseReference mychild = mydb.child("usuarios/" + keyencontrada);
                                            mychild.child("crono").setValue("C/"+(diasRestantes-1)+"/"+fechaInicio+"/"+fecha);
                                        }
                                        //si ya se ha modificado la fecha de cuarentena
                                    }else{
                                        txt="Quedan "+diasRest+" días de cuarentena";
                                        infoEstado.setText(txt);
                                        foto.setImageResource(R.drawable.emojicuarentena);
                                    }
                                    //no hay cuarentena
                                }else{
                                    txt="Todo tranquilo...";
                                    infoEstado.setText(txt);
                                    foto.setImageResource(R.drawable.emojifeliz);
                                }
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}