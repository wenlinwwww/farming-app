package com.example.aq_bluering;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class UsernameShare extends ViewModel {
    // LiveData to hold the 'name' variable
    private final MutableLiveData<String> username = new MutableLiveData<>();

    // Getter for LiveData
    public MutableLiveData<String> getUsername() {
        return username;
    }

    // Setter to update 'name'
    public void setUsername(String name) {
        username.setValue(name);
    }

}
