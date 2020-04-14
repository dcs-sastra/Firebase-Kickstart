package com.drinkkwater.introtofirebase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.roger.catloadinglibrary.CatLoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstFragment extends Fragment {
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    RecyclerView messagesRecyclerView ;
    CatLoadingView catLoadingView;
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
                            List<Messages> messages = new ArrayList<>();
                            Toast.makeText(getContext(),"Task sucessful",Toast.LENGTH_LONG).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Messages message = document.toObject(Messages.class);
                                messages.add(message);
                            }
                            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            messagesRecyclerView.setAdapter(new MessagesAdapter(messages));
                        }
                    }
                });
    }
}
