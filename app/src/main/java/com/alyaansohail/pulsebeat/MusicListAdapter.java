package com.alyaansohail.pulsebeat;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ContentFrameLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>{
    ArrayList<MusicObject> musicList;
    Context context;
    private boolean jamClick;
    int recentlyPlayedMusicId;
    MusicListAdapter(ArrayList<MusicObject> msCons, Context context){
        jamClick = false;
        recentlyPlayedMusicId = -1;
        this.musicList = msCons;
        this.context = context;
    }
    public void addItem(MusicObject msObj){
        musicList.add(msObj);
        notifyItemInserted(musicList.toArray().length);
    }
    protected void jamController(boolean o){
        jamClick = o;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listLayout = layoutInflater.inflate(R.layout.music_layout_adpater_part,parent,false);
        return new ViewHolder(listLayout);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println(musicList.get(0).getMsMusicId());
        holder.nameTextView.setText(musicList.get(position).getMsName());
        holder.byTextView.setText(musicList.get(position).getMsAuthor());
        musicList.get(position).setItemId(holder.getLayoutPosition());
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!jamClick){
                    holder.progressBar.setVisibility(View.VISIBLE);
                    ((MainActivity)context).playMusic(
                            musicList.get(position).getMsUrl(),
                            musicList.get(position).getMsName(),
                            musicList.get(position).getMsAuthor(),
                            musicList.get(position).getMsUpTime(),
                            musicList.get(position).getMsColTheme1(),
                            musicList.get(position).getMsColTheme2(),
                            musicList.get(position).getMsMusicId(),
                            0,
                            holder.getLayoutPosition(),
                            recentlyPlayedMusicId);
                            jamController(true);
                }
            }
        });
        holder.addToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!jamClick){

                    ((MainActivity)context).playMusic(
                            musicList.get(position).getMsUrl(),
                            musicList.get(position).getMsName(),
                            musicList.get(position).getMsAuthor(),
                            musicList.get(position).getMsUpTime(),
                            musicList.get(position).getMsColTheme1(),
                            musicList.get(position).getMsColTheme2(),
                            musicList.get(position).getMsMusicId(),
                            1,
                            holder.getLayoutPosition(),
                            recentlyPlayedMusicId);
                    jamController(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        ProgressBar progressBar;
        TextView byTextView;
        ImageButton playButton;
        ImageButton addToQueue;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.nameTextView = itemView.findViewById(R.id.music_name);
            this.progressBar = itemView.findViewById(R.id.loading_bar);
            this.byTextView = itemView.findViewById(R.id.music_by);
            this.playButton = itemView.findViewById(R.id.play_compo);
            this.addToQueue = itemView.findViewById(R.id.add_to_queue);
        }
    }

    public int getByMusicId(int musicId){
        for(int i = 0; i<musicList.size(); i++){
            if(musicId == musicList.get(i).getMsMusicId()){
                return i;
            }
        }
        return -1;
    }
    public MusicObject getMusicObjByMsId(int musicId){
        for(int i = 0; i<musicList.size(); i++){
            if(musicId == musicList.get(i).getMsMusicId()){
                return musicList.get(i);
            }
        }
        return new MusicObject();
    }
    public void setRecentlyPlayedMusicId(int idLastPlayerMs){
        recentlyPlayedMusicId = idLastPlayerMs;
    }
    public int getRecentlyPlayerMusicID(){
        if(recentlyPlayedMusicId > -1){
            return recentlyPlayedMusicId;
        }else{
            return -1;
        }

    }
}
