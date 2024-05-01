package lk.flavourdash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    public void setCurrentUser(FirebaseUser user) {
        currentUser.setValue(user);
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }
}
