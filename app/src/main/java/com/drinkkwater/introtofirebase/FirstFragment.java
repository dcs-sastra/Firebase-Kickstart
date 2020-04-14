package com.drinkkwater.introtofirebase;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.roger.catloadinglibrary.CatLoadingView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FirstFragment extends Fragment implements MessagesAdapter.OnMessageClicklistner {
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    RecyclerView messagesRecyclerView ;
    CatLoadingView catLoadingView;
    List<Messages> messages = new ArrayList<>();
    FirebaseStorage storage  = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    String typeofdata;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        catLoadingView = new CatLoadingView();
        catLoadingView.setCanceledOnTouchOutside(false);
        catLoadingView.show(getChildFragmentManager(),"Loading");
        catLoadingView.setText(".....");
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
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
        database.collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            catLoadingView.dismiss();
                            Toast.makeText(getContext(),"Task sucessful",Toast.LENGTH_LONG).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Messages message = document.toObject(Messages.class);
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
        final StorageReference fileRef = storageRef.child(url);
        final File rootPath = new File(Environment.getExternalStorageDirectory(), "Firebase-Kickstart");
        Log.w("download", fileRef.getPath());
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
                typeofdata = storageMetadata.getContentType().toString();
                typeofdata = typeofdata.replaceFirst(".*/(\\w+)", "$1");
                Toast.makeText(getContext(), typeofdata, Toast.LENGTH_LONG).show();
                downloadfile(rootPath,fileRef,typeofdata);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

    }
    private void downloadfile(File rootPath,StorageReference fileRef,String typeofdata){
        if(typeofdata != null){
            final File localFile = new File(rootPath, fileRef.getName() + "." + typeofdata);
            try {
                catLoadingView.show(getChildFragmentManager(), "Downloading");
                catLoadingView.setText("Downloading...");
                fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        catLoadingView.dismiss();
                        Toast.makeText(getContext(), "Your file is saved in " + localFile.toString(), Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        catLoadingView.dismiss();
                        Toast.makeText(getContext(), "download task failed", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.w("exception", e);
            }
        }
    }
}
