package com.drinkkwater.introtofirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.roger.catloadinglibrary.CatLoadingView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SecondFragment extends Fragment {

    private FirebaseFirestore dataBase  = FirebaseFirestore.getInstance();
    Map<String,Object> messages = new HashMap<>();
    EditText message_et,author_et;
    CatLoadingView catLoadingView;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    int CHOOSE_FILE = 71;
    Uri filePath;
    Button save;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messages.put("url",null);
        Button extras = view.findViewById(R.id.addfiles);
        extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectfile();
            }
        });
        catLoadingView = new CatLoadingView();
        catLoadingView.setCanceledOnTouchOutside(false);
        catLoadingView.setText(".....");
        message_et = view.findViewById(R.id.message_et);
        author_et = view.findViewById(R.id.author_et);
        save = view.findViewById(R.id.button_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catLoadingView.show(getChildFragmentManager(),"Loading");
                messages.put("message",message_et.getText().toString());
                messages.put("author",author_et.getText().toString());
                senddatatofirestore(messages);
            }
        });

    }
    private void senddatatofirestore(Map<String,Object> messages){
        dataBase.collection("messages")
                .add(messages)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"Success Message uploaded",Toast.LENGTH_LONG).show();
                        catLoadingView.dismiss();
                        NavHostFragment.findNavController(SecondFragment.this)
                                .navigate(R.id.action_SecondFragment_to_FirstFragment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        catLoadingView.dismiss();
                        Toast.makeText(getContext(),"Failed please try again",Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void selectfile(){
        Intent fileIntent = new Intent();
        fileIntent.setType( "*/ *");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(fileIntent,"Choose file to upload"),CHOOSE_FILE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_FILE && data != null && data.getData() != null )
        {
            filePath = data.getData();
            uploadfile();
            Toast.makeText(getContext(),filePath.toString(),Toast.LENGTH_LONG).show();
        }
    }
    private void uploadfile(){
        if(filePath != null)
        { String fileid = UUID.randomUUID().toString();
            save.setClickable(false);
            final StorageReference ref = storageReference.child("files/"+ fileid);
            UploadTask uploadTask = ref.putFile(filePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    Toast.makeText(getContext(),ref.getDownloadUrl().toString(),Toast.LENGTH_LONG).show();
                    Log.w("uploadfile",ref.getDownloadUrl().toString());
                    return ref.getDownloadUrl();
                }
            });
            messages.put("url",urlTask.toString());
            uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    save.setClickable(true);
                }
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                        }
                    });
        }
    }
}
