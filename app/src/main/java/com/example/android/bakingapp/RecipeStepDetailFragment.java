package com.example.android.bakingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.example.android.bakingapp.model.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;


import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.gson.Gson;


import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

public class RecipeStepDetailFragment extends Fragment implements Player.EventListener {

    public static final String ARG_DATA = "step_data";

    private Step step;
    private SimpleExoPlayer player;
    private String videoUrl;
    private Uri videoUri;

    SimpleExoPlayerView playerView;

    long playbackPosition;

    boolean PORTRAIT;
    private boolean mTwoPane = false;
    private Integer position, length;

    private Bundle bundle;

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @BindView(R.id.recipe_detail)
    TextView tv_description;
    @BindView(R.id.player_container)
    LinearLayout playerContainer;
    @BindView(R.id.btn_prev)
    Button btnPrev;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.thumbnail)
    ImageView thumbnail;

    public RecipeStepDetailFragment() {}



    @Override
    public void onCreate(Bundle save) {
        super.onCreate(save);
        setRetainInstance(true);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_detail, container, false);
        ButterKnife.bind(this,rootView);
        bundle = getArguments();




        if (savedInstanceState != null) {
            Log.e(TAG, "(saveInstanceState)get current position : " + playbackPosition);
            playbackPosition = savedInstanceState.getLong("playback_position");
        }

        if (bundle !=null && bundle.containsKey(ARG_DATA)){



            step = new Gson().fromJson(
                    getArguments().getString(ARG_DATA),
                    Step.class
            );
            videoUrl = step.getVideoURL();
            videoUri = Uri.parse(videoUrl);

            position = getArguments().getInt("POSITION");
            length = getArguments().getInt("LENGTH");
            mTwoPane = getArguments().getBoolean("TWOPANE");
        }
        playerView = rootView.findViewById(R.id.video_view);


        if (step != null) {
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || mTwoPane || videoUrl.equals("")) {
                if(mTwoPane) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    playerContainer.setLayoutParams(layoutParams);
                    thumbnail.setLayoutParams(layoutParams);
                }
                tv_description.setVisibility(View.VISIBLE);
                tv_description.setText(step.getDescription());
            } else {
                fullScreen();
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                tv_description.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                btnPrev.setVisibility(View.GONE);
            }


        }
        if(!TextUtils.isEmpty(videoUrl)) {
            initMediaSession();
            initPlayer(videoUri);

        }
        if (player != null && playbackPosition != 0) {
            player.seekTo(playbackPosition);
            player.setPlayWhenReady(true);
        }



        btnNext.setOnClickListener(v -> {
            position++;
            String data = RecipeIngredientStepListActivity.getStep(position);
            step = new Gson().fromJson(data, Step.class);
            Log.e("next data", data);
            setupNavAction();
        });

        btnPrev.setOnClickListener(v -> {
            position--;
            String data = RecipeIngredientStepListActivity.getStep(position);
            step = new Gson().fromJson(data, Step.class);
            Log.e("prev data", data);
            setupNavAction();

        });

        return rootView;
    }




    private void checkNav() {
        if(position==0) {
            btnPrev.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        } else if (position==length-1) {
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.INVISIBLE);

        } else {
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void initPlayer(Uri uri) {
        checkNav();
        videoUrl = step.getVideoURL();
        if(!TextUtils.isEmpty(videoUrl)) {

            thumbnail.setVisibility(View.GONE);

            playerView.setVisibility(View.VISIBLE);
            playerContainer.setVisibility(View.VISIBLE);
            if (player == null) {

                TrackSelector trackSelector = new DefaultTrackSelector();
                LoadControl loadControl = new DefaultLoadControl();

                player = ExoPlayerFactory.newSimpleInstance(
                        this.getContext(),
                        trackSelector, loadControl);

                playerView.setPlayer(player);
                player.addListener(this);
                MediaSource mediaSource = buildMediaSource(uri);
                player.prepare(mediaSource, true, false);
                player.setPlayWhenReady(true);
                player.seekTo(playbackPosition);


            } else {
                MediaSource mediaSource = buildMediaSource(uri);
                player.prepare(mediaSource, false, false);
                player.setPlayWhenReady(true);
                player.seekTo(playbackPosition);
            }


        } else {
            releasePlayer();
            playerView.setVisibility(View.GONE);
            playerContainer.setVisibility(View.GONE);



            if(!TextUtils.isEmpty(step.getThumbnailURL())) {
                thumbnail.setVisibility(View.VISIBLE);
                Context context = thumbnail.getContext();
                Glide.with(context).load(step.getThumbnailURL()).into(thumbnail);

                }
            }



    }


    private void hideSystemUI() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        //fullscreen in landscape
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.removeListener(this);
            player.release();
            player = null;
        }

        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle currentState) {

        if(player!=null){
            currentState.putLong("playback_position",  player.getCurrentPosition());
            currentState.putBundle(ARG_DATA, bundle);
        }
    }





    private void fullScreen() {
        hideSystemUI();
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
    }


    private void setupNavAction() {

        initPlayer(Uri.parse(step.getVideoURL()));
        tv_description.setText(step.getDescription());
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    /*@Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if((playbackState == Player.STATE_READY) && playWhenReady)
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition(), 1f);
        else if((playbackState == Player.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition(), 1f);
        }else{
        mMediaSession.setPlaybackState(mStateBuilder.build());

        }
    }*/
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String stateString;
        switch (playbackState) {

            case Player.STATE_IDLE:
                stateString = "ExoPlayer.STATE_IDLE      -";

                break;
            case Player.STATE_BUFFERING:
                stateString = "ExoPlayer.STATE_BUFFERING -";
                break;
            case Player.STATE_READY:
                stateString = "ExoPlayer.STATE_READY     -";

                break;
            case Player.STATE_ENDED:
                stateString = "ExoPlayer.STATE_ENDED     -";
                break;
            default:
                stateString = "UNKNOWN_STATE             -";

                break;
        }
        Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }
    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }
    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    private void initMediaSession() {
        mMediaSession = new MediaSessionCompat((getContext()), TAG);
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSession.setMediaButtonReceiver(null);
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new BakingAppCallback());
        mMediaSession.setActive(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player!=null){
            //player.getCurrentPosition();
            releasePlayer();
        }
        Log.e(TAG, "(onDestroy) get current position : " + playbackPosition);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(player != null){
            //player.getCurrentPosition();
            initPlayer(videoUri);
            player.seekTo(playbackPosition);
        }

        Log.e(TAG, "(onStart) get current position : " + playbackPosition);
    }


    /*@Override
    public void onResume() {
        super.onResume();

        if(player != null){
            initPlayer(videoUri);
            //player.getCurrentPosition();
            player.seekTo(playbackPosition);

        }

        Log.e(TAG, "(onResume) get current position : " + playbackPosition);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        if (videoUri != null) {
            if (player != null) {
                playbackPosition = player.getCurrentPosition();
                player.seekTo(playbackPosition);
            } else {
                initPlayer(videoUri);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(player!=null){
            //player.getCurrentPosition();

        }
        Log.e(TAG, "(onPause) get current position : " + playbackPosition);
        /*if (player != null && player.getPlayWhenReady()) {
            Log.e(TAG, "(onPause) get current position : " + playbackPosition);
            releasePlayer();
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        if(player!= null){
            player.getCurrentPosition();
            releasePlayer();
        }
        Log.e(TAG, "(onStop) get current position : " + playbackPosition);
        //if (Util.SDK_INT > 23) {

        //}
    }


    private class BakingAppCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            player.setPlayWhenReady(true);
        }

        @Override
        public void onPause() { player.setPlayWhenReady(false); }

        @Override
        public void onSkipToPrevious() {
            player.seekTo(0);
        }
    }

    public class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }



}