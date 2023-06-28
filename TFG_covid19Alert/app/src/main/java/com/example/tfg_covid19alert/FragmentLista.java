package com.example.tfg_covid19alert;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.tfg_covid19alert.pojo.LugarPojo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class FragmentLista extends ListFragment {
    //variables globales
    DatabaseReference mydb = FirebaseDatabase.getInstance().getReference();
    private OnFragmentInteractionListener mListener;
    ArrayList<String> listadoLugares = new ArrayList();
    //Listado de lugares
    ListView lv_listado;
    String emaildni = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String dni = emaildni.substring(0, 9);

    /**Método constructor
     */
    public FragmentLista() {
        // Required empty public constructor
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_lista, container, false);

        //Se agregan elementos de los lugares al array
        final ArrayAdapter<String>array_adaptador = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,listadoLugares);
        lv_listado = view.findViewById(android.R.id.list);
        lv_listado.setAdapter(array_adaptador);

        DatabaseReference locusers = FirebaseDatabase.getInstance().getReference().child("locxusers");
        locusers.addChildEventListener(new ChildEventListener() {
            @Override
            //se busca el numero del local
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String keydeesto = dataSnapshot.getKey();
                locusers.child(keydeesto).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        String quehay = ds.getValue().toString();
                        if(quehay.contains(dni)){
                            String numlocal = ""+quehay.charAt(1); //se obtiene el local (segundo digito string)

                            DatabaseReference locales = FirebaseDatabase.getInstance().getReference().child("locales");
                            locales.child(numlocal).addValueEventListener(new ValueEventListener() {
                                @Override
                                //se obtienen los datos de ese local
                                public void onDataChange(@NonNull DataSnapshot ds1) {
                                    LugarPojo datoslugar = ds1.getValue(LugarPojo.class);
                                    String nombre = datoslugar.getNombre();

                                    //se separa entre - el dia y el mes
                                    String fecha1 = dataSnapshot.getKey();
                                    String[] parts = fecha1.split("-");
                                    String dia = parts[0];
                                    String mes = parts[1];

                                    //depende del numero de mes se guarda el nombre del mes
                                    if(Integer.valueOf(dia)<10)
                                        dia= "0"+dia;

                                    String[] nombresMeses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                                    mes = nombresMeses[Integer.parseInt(mes)];
                                    String fechadeverdad = mes + " - "+ dia;

                                    //Se agrega al arraylist para mostrarlo en el fragment
                                    listadoLugares.add("\n"+fechadeverdad + " \n\n\t                        "+nombre + "\n");
                                    array_adaptador.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                array_adaptador.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
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
