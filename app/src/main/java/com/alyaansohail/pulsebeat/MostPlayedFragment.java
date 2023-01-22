package com.alyaansohail.pulsebeat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class MostPlayedFragment extends Fragment implements Callback{

    Bundle musicDataBundle;
    MusicListAdapter musicListAdapter;
    RecyclerView adapterContainer;

    public MostPlayedFragment(Bundle bndl) {

        System.out.println(bndl);
        this.musicDataBundle = bndl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_most_played, container, false);

        musicListAdapter = new MusicListAdapter(new ArrayList<>(),getContext());

        adapterContainer = view.findViewById(R.id.adapter_container_recycler_view);
        adapterContainer.setHasFixedSize(false);
        adapterContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterContainer.setAdapter(musicListAdapter);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String arrayMusicStr = musicDataBundle.getString("objectMain");
        System.out.println(arrayMusicStr);

        JsonArray arrayMusic = (JsonArray) JsonParser.parseString(arrayMusicStr);

        for(int i = 0;i<arrayMusic.size();i++){
            MusicObject msObj = new Gson().fromJson(arrayMusic.get(i),MusicObject.class);
            musicListAdapter.addItem(msObj);
        }

    }
    public void changeItemOnSelected(int itemId,int typeRes){
        RecyclerView.ViewHolder one =  adapterContainer.findViewHolderForLayoutPosition(itemId);
        assert one != null;
        ImageButton o = one.itemView.findViewById(R.id.play_compo);
        ProgressBar o1 = one.itemView.findViewById(R.id.loading_bar);
        if(typeRes == 1){
            o.setImageResource(R.drawable.ic_baseline_stop_24);
            o1.setVisibility(View.INVISIBLE);
        }else if(typeRes == 2){
            o.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            o1.setVisibility(View.INVISIBLE);
        }


    }
    public void changeLastPlayedCompoToPlay(int idLast){
        int lastPosition = musicListAdapter.getByMusicId(idLast);
        if(lastPosition > -1){
            changeItemOnSelected(lastPosition,2);
            System.out.println("last played changeLastPlayedCompoToPlay" + lastPosition);
        }else {
            System.out.println("less than -1 on changeLastPlyaed on changeLastPlayedCompoTpoPlay" + lastPosition);
        }
    }

    @Override
    public void callStopIcon(int id,int plyMsId) {
        changeItemOnSelected(id,1);
        musicListAdapter.jamController(false);
        musicListAdapter.setRecentlyPlayedMusicId(plyMsId);
    }

    @Override
    public void callChangeLastPlayedIcon(int lastId) {
        changeLastPlayedCompoToPlay(lastId);
        System.out.println(lastId + "On callChangeLastPlayedIcon");
    }

    @Override
    public MusicObject getByMusicIdToMainActivity(int MusicId) {
        return musicListAdapter.getMusicObjByMsId(MusicId);
    }
}