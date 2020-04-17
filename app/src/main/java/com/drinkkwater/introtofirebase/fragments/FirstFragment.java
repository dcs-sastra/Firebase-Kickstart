package com.drinkkwater.introtofirebase.fragments;

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

import com.drinkkwater.introtofirebase.MessageModel;
import com.drinkkwater.introtofirebase.MessagesAdapter;
import com.drinkkwater.introtofirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

    //Create an Instance of Firebase Firestore
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    //Create an Instance of Firebase Storage
    private FirebaseStorage storage  = FirebaseStorage.getInstance();
    //Get reference to Storage
    private StorageReference storageRef = storage.getReference();

    private String typeofdata;

    // UI components
    private RecyclerView messagesRecyclerView ;
    private ExtendedFloatingActionButton floatingActionButton;
    private ProgressBar progressBar;

    /*
        This method inflates the fragment on the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialization of UI components
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        progressBar = view.findViewById(R.id.progressbar);
        floatingActionButton = view.findViewById(R.id.eFab);

        //Setting up onClickListener for the ExtendedFloatingActionButton (Add action)
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigate to the SecondFagment
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        //To get data from Firestore
        datafromfirestore();

    }



    /*
        Callback function when the message item is clicked.
     */
    @Override
    public void OnMessageItemClicked(int position, List<MessageModel> messagesList ) {
        String url = messagesList.get(position).getUrl();
        if(url != null){downloadfiledata(url);}
    }


    /*
        HELPER FUNCTIONS
     */
    private void datafromfirestore(){

        final List<MessageModel> messagesList = new ArrayList<>();
        //Make the progress bar visible
        progressBar.setVisibility(View.VISIBLE);

        database.collection("messages")                                //refers to the "messages" collection in the database
                .get()                                                              //gets all  the data
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {    //called when the data is received
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //Make the progress bar invisible
                        progressBar.setVisibility(View.INVISIBLE);

                        if(task.isSuccessful()){
                            /*
                                Documents in the collection are converted to objects of Messages class
                                Messages class contains same parameters as documents
                             */
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MessageModel message = document.toObject(MessageModel.class);
                                messagesList.add(message);
                            }
                            //Set the RecyclerView and set its adapter
                            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            messagesRecyclerView.setAdapter(new MessagesAdapter(messagesList, FirstFragment.this));
                        }
                    }
                });
    }

    private void downloadfile(File rootPath, StorageReference fileRef, String typeofdata) {

        if (typeofdata != null) {

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

}
