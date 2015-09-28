package com.spotify.sdliles.spotifystreamer.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.sdliles.spotifystreamer.Models.ParcelableArtist;
import com.spotify.sdliles.spotifystreamer.Models.ParcelableTrack;
import com.spotify.sdliles.spotifystreamer.R;
import com.spotify.sdliles.spotifystreamer.Service.MediaPlayerService;
import com.spotify.sdliles.spotifystreamer.Service.MediaPlayerService.MediaPlayerServiceBinder;
import com.spotify.sdliles.spotifystreamer.Service.MediaPlayerService.ServiceState;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
    public static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String BUNDLE_KEY_IS_MEDIA_PLAYER_SERVICE_BOUND = "is-media-player-service-bound";
    public static final int MAX_TRACk_LENGTH_IN_SECONDS = 30;
    private MediaPlayerServiceBinder mMediaPlayerServiceBinder;
    private View mRootView;
    private ActionBar mActionBar;
    private Activity mParentActivity;
    private ParcelableArtist mArtist;
    private Integer mPosition;
    private ArrayList<ParcelableTrack> mTracks;
    private MediaPlayerService mMediaPlayerService;
    private boolean mIsMediaPlayerServiceBound = false;

    private ImageButton mPreviousButton;
    private ImageButton mPlayPauseButton;
    private ImageButton mFowardButton;
    private SeekBar mSeekBar;

    private ServiceConnection mMediaPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaPlayerServiceBinder = (MediaPlayerServiceBinder) service;
            mMediaPlayerService = mMediaPlayerServiceBinder.getService();

            setPlayPauseButtonState(mMediaPlayerService.getState());

            if (mIsMediaPlayerServiceBound) {
                if (mPosition != mMediaPlayerService.getPosition()) {
                    mPosition = mMediaPlayerService.getPosition();
                    bindValues();
                }
            } else {
                mMediaPlayerService.setArtist(mArtist);
                mMediaPlayerService.setTracksList(mTracks);
                mMediaPlayerService.selectTrack(mPosition);
                mMediaPlayerService.startPlay();
                mIsMediaPlayerServiceBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsMediaPlayerServiceBound = false;
        }
    };

    private BroadcastReceiver mOnCurrentTrackChangedReceiver;
    private BroadcastReceiver mOnCurrentStateChangedReceiver;
    private BroadcastReceiver mOnTrackProgressUpdatedReceiver;

    private void setPlayPauseButtonState(ServiceState state) {
        if (ServiceState.PLAYING.equals(state)) {
            playPauseButton().setImageResource(android.R.drawable.ic_media_pause);
        } else if (ServiceState.PAUSED.equals(state) || ServiceState.STOPPED.equals(state)) {
            playPauseButton().setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private ImageButton playPauseButton() {
        return (ImageButton) mRootView.findViewById(R.id.player_play_pause_button);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_player, container, false);
        mParentActivity = getActivity();

        if (savedInstanceState == null) {
            loadArguments();
        } else {
            loadSavedInstanceState(savedInstanceState);
        }

        setupMediaPlayerService();

        setupActionBar();
        bindValues();

        setupControlButtons();
        setupSeekBar();

        setupBroadcastReceivers();

        return mRootView;
    }

    private void setupControlButtons() {
        mPreviousButton = previousButton();
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerService.previous();
            }
        });

        mPlayPauseButton = playPauseButton();
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerService.togglePlayback();
            }
        });

        mFowardButton = nextButton();
        mFowardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerService.foward();
            }
        });
    }

    private void setupSeekBar() {
        mSeekBar = seekBar();
        mSeekBar.setProgress(0);
        mSeekBar.setMax(30);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private ImageButton nextButton() {
        return (ImageButton) mRootView.findViewById(R.id.player_next_button);
    }

    private ImageButton previousButton() {
        return (ImageButton) mRootView.findViewById(R.id.player_previous_button);
    }

    private SeekBar seekBar() {
        return (SeekBar) mRootView.findViewById(R.id.player_track_progress_seek_bar);
    }

    private void enablePlayerControls() {
        playPauseButton().setEnabled(true);
        nextButton().setEnabled(true);
        previousButton().setEnabled(true);
        seekBar().setEnabled(true);
    }

    private void disablePlayerControls() {
        playPauseButton().setEnabled(false);
        nextButton().setEnabled(false);
        previousButton().setEnabled(false);
        seekBar().setEnabled(false);
    }

    private void setupBroadcastReceivers() {
        LocalBroadcastManager.getInstance(mParentActivity).registerReceiver(getOnCurrentTrackChangedReceiver(),
                new IntentFilter(MediaPlayerService.BROADCAST_CURRENT_TRACK_CHANGED));

        LocalBroadcastManager.getInstance(mParentActivity).registerReceiver(getOnCurrentStateChangedReceiver(),
                new IntentFilter(MediaPlayerService.BROADCAST_PLAYER_STATE_CHANGED));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(getOnTrackProgressUpdatedReceiver(),
                new IntentFilter(MediaPlayerService.BROADCAST_TRACK_PLAYBACK_CURRENT_PROGRESS));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(ArtistSearchFragment.ARTIST_KEY, mArtist);
        bundle.putParcelableArrayList(TopTracksFragment.TRACKS_KEY, mTracks);
        bundle.putInt(TopTracksFragment.SELECTED_TRACK_INDEX, mPosition);
        bundle.putBoolean(BUNDLE_KEY_IS_MEDIA_PLAYER_SERVICE_BOUND, mIsMediaPlayerServiceBound);
    }

    private void loadSavedInstanceState(Bundle savedInstanceState) {
        mArtist = savedInstanceState.getParcelable(ArtistSearchFragment.ARTIST_KEY);
        mTracks = savedInstanceState.getParcelableArrayList(TopTracksFragment.TRACKS_KEY);
        mPosition = savedInstanceState.getInt(TopTracksFragment.SELECTED_TRACK_INDEX);
        mIsMediaPlayerServiceBound = savedInstanceState.getBoolean(BUNDLE_KEY_IS_MEDIA_PLAYER_SERVICE_BOUND);
    }

    private void setupMediaPlayerService() {
        Context context = mParentActivity.getApplicationContext();
        Intent intent = new Intent(context, MediaPlayerService.class);
        context.startService(intent);
        context.bindService(intent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void loadArguments() {
        Bundle arguments = getArguments();
        mArtist = arguments.getParcelable(ArtistSearchFragment.ARTIST_KEY);
        mTracks = arguments.getParcelableArrayList(TopTracksFragment.TRACKS_KEY);
        mPosition = arguments.getInt(TopTracksFragment.SELECTED_TRACK_INDEX);
    }

    private void setupActionBar() {
        mActionBar = actionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private ActionBar actionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void bindValues() {
        TextView artistNameTextView = (TextView) mRootView.findViewById(R.id.player_artist_name);
        TextView albumNameTextView = (TextView) mRootView.findViewById(R.id.player_album_name);
        TextView trackNameTextView = (TextView) mRootView.findViewById(R.id.player_track_name);
        ImageView albumImageImageView = (ImageView) mRootView.findViewById(R.id.player_album_art);

        ParcelableTrack currentTrack = currentTrack();
        artistNameTextView.setText(mArtist.getName());
        albumNameTextView.setText(currentTrack.getAlbumName());
        trackNameTextView.setText(currentTrack.getTrackName());

        Picasso.with(mParentActivity)
                .load(currentTrack.getAlbumArtLargeUrl())
                .into(albumImageImageView);

        mActionBar = actionBar();
        mActionBar.setTitle(mArtist.getName());
        mActionBar.setSubtitle(currentTrack.getTrackName());
    }

    private ParcelableTrack currentTrack() {
        return mTracks.get(mPosition);
    }


    private BroadcastReceiver getOnCurrentTrackChangedReceiver() {
        if (mOnCurrentStateChangedReceiver != null) {
            return mOnCurrentTrackChangedReceiver;
        }

        mOnCurrentTrackChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    Log.d(LOG_TAG, "CurrentTrackChanged broadcast received.");
                    mPosition = intent.getIntExtra(MediaPlayerService.BROADCAST_DATA_CURRENT_TRACK, 0);
                    Log.d(LOG_TAG, String.format("Current track index: %s", mPosition));
                    bindValues();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        return mOnCurrentTrackChangedReceiver;
    }

    private BroadcastReceiver getOnCurrentStateChangedReceiver() {
        if (mOnCurrentStateChangedReceiver != null) {
            return mOnCurrentStateChangedReceiver;
        }

        mOnCurrentStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    Log.d(LOG_TAG, "CurrentStateChanged broadcast received.");
                    ServiceState state = (ServiceState) intent.getSerializableExtra(MediaPlayerService.BROADCAST_DATA_CURRENT_STATE);
                    Log.d(LOG_TAG, String.format("Current state: %s", state));

                    setPlayPauseButtonState(state);

                    if (ServiceState.PREPARING.equals(state)) {
                        disablePlayerControls();
                    } else {
                        enablePlayerControls();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        return mOnCurrentStateChangedReceiver;
    }

    private BroadcastReceiver getOnTrackProgressUpdatedReceiver() {
        if (mOnTrackProgressUpdatedReceiver != null) {
            return mOnTrackProgressUpdatedReceiver;
        }

        mOnTrackProgressUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    int progress = intent.getIntExtra(MediaPlayerService.BROADCAST_DATA_CURRENT_PROGRESS, 0);
                    progress = (int) Math.ceil((double) progress / 1000);
                    if (progress > MAX_TRACk_LENGTH_IN_SECONDS) {
                        progress = MAX_TRACk_LENGTH_IN_SECONDS;
                    }

                    Log.v(LOG_TAG, String.format("Current progress: %s", progress));
                    textviewCurrentProgress().setText(String.format("0:%02d", progress));
                    seekBar().setProgress(progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        return mOnTrackProgressUpdatedReceiver;
    }

    private TextView textviewCurrentProgress() {
        return (TextView) mRootView.findViewById(R.id.player_track_current_progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // should be in milliseconds.
            int position = progress * 1000;
            mMediaPlayerService.seekTo(position);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}