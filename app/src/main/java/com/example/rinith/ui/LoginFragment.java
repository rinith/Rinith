package com.example.rinith.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rinith.R;
import com.example.rinith.databinding.FragmentLoginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.truecaller.android.sdk.TruecallerSDK;
import com.truecaller.android.sdk.TruecallerSdkScope;


public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private DatabaseReference myRef;

    FragmentLoginBinding binding;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        view = binding.getRoot();
        initData();
        setUpSharedPref();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        return view;
    }

    private void setUpSharedPref() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String mobile_number = sharedPref.getString(getString(R.string.mobile_number), "");
        if (mobile_number.isEmpty()) {
            setUpTrueCaller();
            getProfile();
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_homeFragment);
        }

    }


    private void initData() {

        binding.verifyBtn.setOnClickListener(view1 -> {
            String number = binding.editText.getText().toString().trim();
            if (!number.isEmpty()) {
                LoginFragmentDirections.ActionLoginFragmentToOptVerificationFragment action = LoginFragmentDirections.actionLoginFragmentToOptVerificationFragment(number);
                action.setMobileNumber(number);
                Navigation.findNavController(view).navigate(action);
            } else {
                Toast.makeText(requireContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readData() {
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void writeData() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }

    private void getProfile() {
        if (TruecallerSDK.getInstance().isUsable()) {
            TruecallerSDK.getInstance().getUserProfile(this);
        }
    }

    private void setUpTrueCaller() {
        TruecallerSdkScope trueScope = new TruecallerSdkScope.Builder(requireContext(), sdkCallback)
                .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
                .buttonColor(Color.BLUE)
                .buttonTextColor(Color.WHITE)
                .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
                .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
                .ctaTextPrefix(TruecallerSdkScope.CTA_TEXT_PREFIX_USE)
                .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
                .privacyPolicyUrl("<<YOUR_PRIVACY_POLICY_LINK>>")
                .termsOfServiceUrl("<<YOUR_PRIVACY_POLICY_LINK>>")
                .footerType(TruecallerSdkScope.FOOTER_TYPE_NONE)
                .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_LOG_IN)
                .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP)
                .build();

        TruecallerSDK.init(trueScope);
    }

    private final ITrueCallback sdkCallback = new ITrueCallback() {

        @Override
        public void onSuccessProfileShared(@NonNull final TrueProfile trueProfile) {
            Toast.makeText(getContext(), "Login Successful!!", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.mobile_number), trueProfile.phoneNumber);
            editor.putString(getString(R.string.user_name), trueProfile.firstName);
            editor.apply();
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
            Navigation.findNavController(view).navigate(action);
        }

        @Override
        public void onFailureProfileShared(@NonNull final TrueError trueError) {

        }

        @Override
        public void onVerificationRequired(@Nullable final TrueError trueError) {

        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TruecallerSDK.getInstance().onActivityResultObtained(requireActivity(), resultCode, data);
    }

}