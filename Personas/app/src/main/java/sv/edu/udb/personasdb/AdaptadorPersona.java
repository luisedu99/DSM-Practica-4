package sv.edu.udb.personasdb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sv.edu.udb.personasdb.datos.Persona;

public class AdaptadorPersona extends AppCompatActivity {

    List<Persona> personas;
    private Activity context;

    public AdaptadorPersona(@NonNull Activity context, @NonNull List<Persona> personas){
        super(context,R.layout.activity_adaptador_persona,personas);
        this.context=context;
        this.personas=personas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent){
        // Método invocado tantas veces como elementos tenga la coleccion personas
        // para formar a cada item que se visualizara en la lista personalizada
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View rowview=null;
        // optimizando las diversas llamadas que se realizan a este método
        // pues a partir de la segunda llamada el objeto view ya viene formado
        // y no sera necesario hacer el proceso de "inflado" que conlleva tiempo y
        // desgaste de bateria del dispositivo
        if (view == null)
            rowview = layoutInflater.inflate(R.layout.activity_adaptador_persona,null);
        else rowview = view;

        TextView tvNombre = rowview.findViewById(R.id.tvNombre);
        TextView tvDUI = rowview.findViewById(R.id.tvDUI);
        TextView tvGenero=rowview.findViewById(R.id.tvGenero);
        TextView tvFecha=rowview.findViewById(R.id.tvFecha);
        TextView tvAltura=rowview.findViewById(R.id.tvAltura);
        TextView tvPeso=rowview.findViewById(R.id.tvPeso);

        tvNombre.setText("Nombre : "+personas.get(position).getNombre());
        tvDUI.setText("DUI : " + personas.get(position).getDui());
        tvGenero.setText("Genero : "+personas.get(position).getGenero());
        tvFecha.setText("Fecha : "+personas.get(position).getFecha());
        tvAltura.setText("Altura : "+personas.get(position).getAltura());
        tvPeso.setText("Peso : "+personas.get(position).getPeso());

        return rowview;
    }
}