package com.curso.asistenciaqr.fragments.maestro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.adapters.AsistenciaAdapter;
import com.curso.asistenciaqr.models.Asistencia;
import com.curso.asistenciaqr.repository.AsistenciaRepository;
import java.util.ArrayList;
import java.util.List;

public class ListaAsistenciaFragment extends Fragment {

    private AsistenciaRepository          asistenciaRepository;
    private MutableLiveData<List<Asistencia>> liveData;
    private AsistenciaAdapter             adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_asistencia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        asistenciaRepository = new AsistenciaRepository();
        liveData = new MutableLiveData<>();
        requireActivity().setTitle("Asistencia");

        String sesionId = getArguments() != null ?
                getArguments().getString("sesionId") : "";

        ProgressBar    progressBar = view.findViewById(R.id.progress_bar);
        TextView       tvContador  = view.findViewById(R.id.tv_contador);
        RecyclerView   recycler    = view.findViewById(R.id.recycler_asistencia);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AsistenciaAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        liveData.observe(getViewLifecycleOwner(), lista -> {
            progressBar.setVisibility(View.GONE);
            adapter.actualizar(lista);
            tvContador.setText(lista.size() + " presentes");
        });

        asistenciaRepository.escucharAsistencias(sesionId, liveData);
    }
}