package com.drinkkwater.introtofirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



public class SecondFragment extends Fragment {

    FirebaseFirestore dataBase  = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    Map<String,Object> messages = new HashMap<>();
    EditText message_et,author_et;
    int CHOOSE_FILE = 71;
    Uri filePath;
    Button save;
    ProgressBar progressBar;

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

        //to add attachments
        Button extras = view.findViewById(R.id.addfiles);
        extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectfile();
            }
        });
        progressBar = view.findViewById(R.id.progressbar);

        message_et = view.findViewById(R.id.message_et);
        author_et = view.findViewById(R.id.author_et);

        save = view.findViewById(R.id.button_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.put("message",message_et.getText().toString());
                messages.put("author",author_et.getText().toString());
                senddatatofirestore(messages);
            }
        });

    }
    private void senddatatofirestore(Map<String,Object> messages){
        dataBase.collection("messages")
                .add(messages)      //add method uploads data to firestore
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"Success Message uploaded",Toast.LENGTH_LONG).show();
                        NavHostFragment.findNavController(SecondFragment.this)
                                .navigate(R.id.action_SecondFragment_to_FirstFragment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
        { progressBar.setVisibility(View.VISIBLE);
            String fileid = UUID.randomUUID().toString(); //random id for file name
            save.setVisibility(View.GONE);
            final StorageReference ref = storageReference.child("files/"+ fileid);   //refers to a specific path here "files/name_of_the_file"
            UploadTask uploadTask = ref.putFile(filePath); //putFile method uploads file to storage
            messages.put("url",ref.getPath());      //path of the file is stored to firestore to download the file later
            uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            save.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Upload Failed",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    save.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"File Uploaded",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
