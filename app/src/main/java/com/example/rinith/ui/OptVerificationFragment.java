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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rinith.R;
import com.example.rinith.databinding.FragmentOptVerificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.truecaller.android.sdk.TruecallerSDK;
import com.truecaller.android.sdk.TruecallerSdkScope;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


public class OptVerificationFragment extends Fragment {

    private String mVerificationId;
    private FirebaseAuth mAuth;
    String number = "";

    FragmentOptVerificationBinding binding;
    View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opt_verification, container, false);
        view = binding.getRoot();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        getData();

        return view;

    }

    private void getData() {
        number = "+91" + OptVerificationFragmentArgs.fromBundle(getArguments()).getMobileNumber();
        binding.textView3.setText("Otp sent to " + number);
        sendVerificationCode(number);

        binding.verifyBtn.setOnClickListener(view1 -> {
            if (binding.enterCodeEditText.length() == 6) {
                verifyVerificationCode(binding.enterCodeEditText.getText().toString());
            } else {
                Toast.makeText(requireContext(), "Enter proper otp", Toast.LENGTH_SHORT).show();
            }

        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // setUp();
        //setUpTrueCaller();
        //  getProfile();
    }

   /* private void setUp() {
        binding.button.setOnClickListener(view -> {
            sendVerificationCode(binding.outlinedExposedDropdownEditable.getText().toString());
        });
    }*/

    private void verifyVerificationCode(String code) {

        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void sendVerificationCode(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNo,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                if (binding.enterCodeEditText != null) {
                    binding.enterCodeEditText.setText(code);
                }
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }

    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            try {
                                //verification successful we will start the profile activity
                                if (task.getResult() != null && task.getResult().getUser() != null) {
                                    //phoneUserId = task.getResult().getUser().getUid();
                                    Toast.makeText(getContext(), "Login Successful!!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(getString(R.string.mobile_number), number);
                                    editor.apply();

                                    NavDirections action = OptVerificationFragmentDirections.actionOptVerificationFragmentToHomeFragment();
                                    Navigation.findNavController(view).navigate(action);
                                }
                            } catch (Exception e1) {
                                // Error occurred while creating the File
                                Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Invalid otp..", Toast.LENGTH_SHORT).show();

                        }

                    });
        }
    }


}