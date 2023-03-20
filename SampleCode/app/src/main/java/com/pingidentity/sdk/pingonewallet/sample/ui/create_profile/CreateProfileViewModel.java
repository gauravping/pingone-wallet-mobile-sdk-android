package com.pingidentity.sdk.pingonewallet.sample.ui.create_profile;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;

public class CreateProfileViewModel extends BaseViewModel {

    private final MutableLiveData<Profile> mProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> mShowDialogLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mCompletionLiveData = new MutableLiveData<>();

    public CreateProfileViewModel(DataRepository dataRepository) {
        super(dataRepository);
        mProfileLiveData.postValue(dataRepository.getProfile());
    }

    public void createProfile(Bitmap selfie, String firstName, String lastName, String email) {
        if (firstName.isEmpty()) {
            mShowDialogLiveData.postValue("You must enter your first name to create the profile.");
            return;
        }

        if (lastName.isEmpty()) {
            mShowDialogLiveData.postValue("You must enter your last name to create the profile.");
            return;
        }

        if (email.isEmpty()) {
            mShowDialogLiveData.postValue("You must enter your email to create the profile.");
            return;
        }

        if (selfie == null) {
            mShowDialogLiveData.postValue("You must capture your selfie to create the profile.");
            return;
        }

        getDataManager().saveProfile(new Profile(firstName, lastName, email, selfie));
        mCompletionLiveData.postValue(true);
    }

    public MutableLiveData<Profile> subscribeProfile() {
        return mProfileLiveData;
    }

    public MutableLiveData<Boolean> subscribeFlowCompletion() {
        return mCompletionLiveData;
    }

    public MutableLiveData<String> subscribeShowDialog() {
        return mShowDialogLiveData;
    }

}
