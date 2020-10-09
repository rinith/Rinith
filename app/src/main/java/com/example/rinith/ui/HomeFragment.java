package com.example.rinith.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.rinith.R;
import com.example.rinith.databinding.HomeFragmentBinding;
import com.example.rinith.viewmodel.HomeViewModel;
import com.example.rinith.viewobject.Member;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    HomeFragmentBinding binding;
    private DatabaseReference mDatabase;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri videoUri;
    SharedPreferences sharedPref;
    private static final int PICK_VIDEO = 1;
    UploadTask uploadTask;
    String userName = "";

    ProgressDialog progressdialog;


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference("Video");
        storageReference = FirebaseStorage.getInstance().getReference("Video");
        mAuth = FirebaseAuth.getInstance();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        progressdialog = new ProgressDialog(requireContext());

        progressdialog.setMessage("Uploading please wait....");

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);

        loadData();
        binding.uploadVideo.setOnClickListener(view -> {
            chooseVideo();
        });
        return binding.getRoot();
    }

    private void loadData() {

        String mobile_number = sharedPref.getString(getString(R.string.mobile_number), "");
        String usr_name = sharedPref.getString(getString(R.string.user_name), "");
        if (usr_name.isEmpty()) {
            binding.mobNum.setText("Welcome " + mobile_number);
            userName = "Usr" + mobile_number.substring(3, 10);
        } else {
            binding.mobNum.setText("Hello " + usr_name);
            userName = usr_name;
        }

        binding.logout.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.mobile_number), "");
            editor.putString(getString(R.string.user_name), "");
            editor.apply();
            Toast.makeText(getContext(), "Logout Successful!!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment);
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

    private void uploadVideo(String name, Uri videoUrl, String userName) {

        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExt(videoUri));

        if (videoUri != null) {
            storageReference.putFile(videoUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        Toast.makeText(requireActivity(), "Data saved", Toast.LENGTH_SHORT).show();
                        uploadTask = reference.putFile(videoUri);
                        Member member = new Member(name, downloadUrl.toString(), userName);
                        String i = mDatabase.push().getKey();
                        mDatabase.child(i).setValue(member);
                        Toast.makeText(requireActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        progressdialog.dismiss();
                    } else {
                        Toast.makeText(requireActivity(), "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String getExt(Uri uri) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO || resultCode == RESULT_OK ||
                data != null || data.getData() != null) {
            videoUri = data.getData();
            progressdialog.show();
            uploadVideo("VID" + String.format("%04d", new Random().nextInt(10000)), videoUri, userName);
        }


    }


    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO);
    }
}