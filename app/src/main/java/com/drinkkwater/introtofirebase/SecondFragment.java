package com.drinkkwater.introtofirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;
import java.util.UUID;



public class SecondFragment extends Fragment {

    // References to Firebase
    private FirebaseFirestore dataBase  = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    private MessageModel messageModel = new MessageModel();
    private static int CHOOSE_FILE = 71;
    private Uri filePath;

    // UI Components
    private TextInputEditText message_et, author_et;
    private MaterialButton save;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialization of UI components
        progressBar = view.findViewById(R.id.progressbar);
        message_et = view.findViewById(R.id.message_et);
        author_et = view.findViewById(R.id.author_et);
        save = view.findViewById(R.id.button_save);
        Button btnAddFiles = view.findViewById(R.id.addfiles);

        messageModel.setUrl(null);

        //Setting up onClickListener to Add Attachments Button
        btnAddFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectfile();
            }
        });

        //Setting up onClickListener to Save Button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = message_et.getText().toString();
                String author = author_et.getText().toString();

                if (! message.isEmpty() && !author.isEmpty()) {
                    messageModel.setMessage(message);
                    messageModel.setAuthor(author);
                    senddatatofirestore(messageModel);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_FILE && data != null && data.getData() != null )
        {
            //Once the file is successfully fetched from the device it's uploaded.
            filePath = data.getData();
            String fileName = filePath.getLastPathSegment();
            uploadfile(fileName);
            Toast.makeText(getContext(),filePath.toString(),Toast.LENGTH_LONG).show();
            Log.d("fff", fileName);
        }
    }


    /*
        HELPER FUNCTIONS
     */
    private void senddatatofirestore(MessageModel messageModel){

        dataBase.collection("messages")
                .add(messageModel)      //add method uploads data to firestore
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"Success Message uploaded",Toast.LENGTH_LONG).show();
                        //This navigate from Second Fragment to First Fragment
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
        // */* - means all file type can be selected
        fileIntent.setType( "*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(fileIntent,"Choose file to upload"), CHOOSE_FILE);
    }

    private void uploadfile(String fileName){

        if(filePath != null) {

            progressBar.setVisibility(View.VISIBLE);
            String fileid = fileName + "_" + UUID.randomUUID().toString(); //random id for file name
            save.setVisibility(View.GONE);

            final StorageReference ref = storageReference.child("files/"+ fileid);   //refers to a specific path here "files/name_of_the_file"
            UploadTask uploadTask = ref.putFile(filePath); //putFile method uploads file to storage

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Log.d("fff", ref.getDownloadUrl().toString());

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();

                        Log.d("fff", downloadUri.toString());

                        messageModel.setUrl(downloadUri.toString());      //path of the file is stored to firestore to download the file later
                        save.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"File Uploaded",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getContext(),"Upload Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    save.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"File Uploaded",Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            save.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Upload Failed",Toast.LENGTH_SHORT).show();
                        }
            });
        }
    }
}
