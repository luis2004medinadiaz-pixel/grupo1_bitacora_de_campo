package com.example.bitacora.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.repository.RegistroRepository;
import com.example.bitacora.ui.UiState;
import java.util.List;

public class RegistroViewModel extends ViewModel {
    private final RegistroRepository repo = new RegistroRepository();

    private final MutableLiveData<UiState<List<RegistroDto>>> state = new MutableLiveData<>(UiState.loading());
    public LiveData<UiState<List<RegistroDto>>> getState() { return state; }

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    public LiveData<String> getActionMessage() { return actionMessage; }

    public void load() {
        state.setValue(UiState.loading());
        repo.getAll(new RegistroRepository.CallbackList() {
            @Override
            public void onResult(List<RegistroDto> data, String error) {
                if (error != null) {
                    state.postValue(UiState.error(error));
                } else if (data == null || data.isEmpty()) {
                    state.postValue(UiState.empty());
                } else {
                    state.postValue(UiState.success(data));
                }
            }
        });
    }

    public void create(RegistroDto body) {
        repo.create(body, new RegistroRepository.CallbackOne() {
            @Override
            public void onResult(RegistroDto data, String error) {
                if (error != null) actionMessage.postValue(error);
                else { actionMessage.postValue("Creado correctamente"); load(); }
            }
        });
    }

    public void update(int id, RegistroDto body) {
        repo.update(id, body, new RegistroRepository.CallbackOne() {
            @Override
            public void onResult(RegistroDto data, String error) {
                if (error != null) actionMessage.postValue(error);
                else { actionMessage.postValue("Actualizado correctamente"); load(); }
            }
        });
    }

    public void delete(int id) {
        repo.delete(id, new RegistroRepository.CallbackVoid() {
            @Override
            public void onResult(String error) {
                if (error != null) actionMessage.postValue(error);
                else { actionMessage.postValue("Eliminado correctamente"); load(); }
            }
        });
    }
}