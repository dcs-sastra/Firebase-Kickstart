package com.drinkkwater.introtofirebase;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FirstFragment extends Fragment implements MessagesAdapter.OnMessageClicklistner {

    //create an Instance of Firebase firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    //create an Instance of Firebase storage
    FirebaseStorage storage  = FirebaseStorage.getInstance();
    //get reference to Storage
    StorageReference storageRef = storage.getReference();

    List<Messages> messages = new ArrayList<>();
    RecyclerView messagesRecyclerView ;
    String typeofdata;
    ProgressBar progressBar;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        progressBar = view.findViewById(R.id.progressbar);
        //To get data from firestore
        datafromfirestore();
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

    }
    private void datafromfirestore(){
        progressBar.setVisibility(View.VISIBLE);
        database.collection("messages")                                //refers to the "messages" collection in the database
                .get()                                                              //gets all  the data
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {    //called when the data is received
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {  //documents in the collection are converted to objects of Messages class
                                Messages message = document.toObject(Messages.class);  //Messages class contains same parameters as documents
                                messages.add(message);
                            }
                            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            messagesRecyclerView.setAdapter(new MessagesAdapter(messages, FirstFragment.this));
                        }
                    }
                });
    }

    @Override
    public void OnClick(int position) {
        String url = messages.get(position).getUrl();
        if(url != null){downloadfiledata(url);}
    }
    public void downloadfiledata(String url) {
        progressBar.setVisibility(View.VISIBLE);
        //url contains the path to file in the database
        //fileRef refers to a specific file
        final StorageReference fileRef = storageRef.child(url);

        //Creates folder to store files
        final File rootPath = new File(Environment.getExternalStorageDirectory(), "Firebase-Kickstart");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        //Metadata is the information about data/file, for example we can know the type of a file (.png/.jpeg/.pdf/...) from metadata
        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                typeofdata = storageMetadata.getContentType().toString();
                //"typeofdata" contains the type eg:png
                typeofdata = typeofdata.replaceFirst(".*/(\\w+)", "$1");

                downloadfile(rootPath,fileRef,typeofdata);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        messages.clear();
    }

    private void downloadfile(File rootPath, StorageReference fileRef, String typeofdata){
        if(typeofdata != null){

            //file is created with the type specified
            final File localFile = new File(rootPath, fileRef.getName() + "." + typeofdata);
            try {
                progressBar.setVisibility(View.VISIBLE);
                //getFile method downloads the data
                fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        //file has been created
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Your file is saved in " + localFile.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "download task failed", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.w("exception", e);
            }
        }
    }
}
