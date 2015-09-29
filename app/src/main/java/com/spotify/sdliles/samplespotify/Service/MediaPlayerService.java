package com.spotify.sdliles.samplespotify.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.Models.ParcelableTrack;
import com.spotify.sdliles.samplespotify.R;
import com.spotify.sdliles.samplespotify.UI.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public static final String BROADCAST_CURRENT_TRACK_CHANGED = "current-track-changed";
    public static final String BROADCAST_PLAYER_STATE_CHANGED = "player-state-changed";
    public static final String BROADCAST_TRACK_PLAYBACK_CURRENT_PROGRESS = "track-playback-current-progress";
    public static final String BROADCAST_DATA_CURRENT_TRACK = "current-track";
    public static final String BROADCAST_DATA_CURRENT_STATE = "current-state";
    public static final String BROADCAST_DATA_CURRENT_PROGRESS = "current-progress";


    public static final String ACTION_TOGGLE_PLAYBACK = "com.spotify.sdliles.samplespotify.action.TOGGLE_PLAYBACK";
    public static final String ACTION_FOWARD = "com.spotify.sdliles.samplespotify.action.FOWARD";
    public static final String ACTION_PREVIOUS = "com.spotify.sdliles.samplespotify.action.PREVIOUS";


    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();
    private static final float LOUD_VOLUME = 1.0f;
    private static final float DUCK_VOLUME = 0.1f;
    private static final int NOTIFICATION_ID = 1;
    private final IBinder selfBinder = new MediaPlayerServiceBinder();
    private MediaPlayer mMediaPlayer;
    private ArrayList<ParcelableTrack> mTracks;
    private int mPosition = 0;
    private WifiManager.WifiLock mWifiLock;
    private AudioManager mAudioManager;
    private AudioFocus mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
    private ServiceState mState = ServiceState.STOPPED;
    private ScheduledExecutorService mScheduleTaskExecutor;
    private ScheduledFuture<?> mTrackProgressReporter;
    private NotificationManager mNotificationManager;
    private ParcelableArtist mArtist;

    boolean mHasError = false;

    @Override
    public void onCreate() {
        super.onCreate();

        setupMediaPlayer();
        createWifiLock();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void setupMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onDestroy() {
        releaseResources(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return selfBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        releaseResources(true);

        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }

        switch (action) {
            case ACTION_TOGGLE_PLAYBACK:
                togglePlayback();
                break;
            case ACTION_FOWARD:
                foward();
                break;
            case ACTION_PREVIOUS:
                previous();
                break;
            default:
                Log.w(LOG_TAG, String.format("Intent action %s doesn't match any known action.", action));
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Workaround for handling errors on prepareAsync.
        if (mHasError) {
            startPlay();
        } else {
            foward();
        }

        mHasError = false;
    }

    public void foward() {
        int lastIndex = mTracks.size() - 1;
        if (mPosition == lastIndex) {
            mPosition = 0;
        } else {
            mPosition++;
        }

        broadcastCurrentTrackIndex();
        startPlay();
    }

    public void previous() {
        int lastIndex = mTracks.size() - 1;
        if (mPosition == 0) {
            mPosition = lastIndex;
        } else {
            mPosition--;
        }

        broadcastCurrentTrackIndex();
        startPlay();
    }

    /**
     * Seek to specific position of the track.
     *
     * @param position milliseconds
     */
    public void seekTo(int position) {
        if (ServiceState.PLAYING.equals(mState) || ServiceState.PAUSED.equals(mState)) {
            mMediaPlayer.seekTo(position);
        }
    }

    public int getPosition() {
        return mPosition;
    }

    public ServiceState getState() {
        return mState;
    }

    private void broadcastCurrentTrackIndex() {
        Intent intent = new Intent(BROADCAST_CURRENT_TRACK_CHANGED);
        intent.putExtra(BROADCAST_DATA_CURRENT_TRACK, mPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void activateProgressReporter() {
        if (mScheduleTaskExecutor == null) {
            mScheduleTaskExecutor = new ScheduledThreadPoolExecutor(1);
        }

        mTrackProgressReporter = mScheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Intent intent = new Intent(BROADCAST_TRACK_PLAYBACK_CURRENT_PROGRESS);
                intent.putExtra(BROADCAST_DATA_CURRENT_PROGRESS, mMediaPlayer.getCurrentPosition());
                LocalBroadcastManager.getInstance(MediaPlayerService.this).sendBroadcast(intent);
            }
        }, 500, 350, TimeUnit.MILLISECONDS);
    }

    private void deactivateProgressReporter() {
        if (mTrackProgressReporter != null) {
            mTrackProgressReporter.cancel(true);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mHasError = true;
        Log.e(LOG_TAG, String.format("MediaPlayer error: [what: %s, extra: %s]", what, extra));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        configAndStartMediaPlayer();

        String text = String.format(getResources().getString(R.string.notification_playing), getCurrentTrack().getTrackName());
        updateNotification(text);
    }

    private void configAndStartMediaPlayer() {
        switch (mAudioFocus) {
            case NO_FOCUS_NO_DUCK:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                return;
            case NO_FOCUS_CAN_DUCK:
                mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
                break;
            default:
                mMediaPlayer.setVolume(LOUD_VOLUME, LOUD_VOLUME);
        }

        if (!mMediaPlayer.isPlaying()) {
            activateProgressReporter();
            mMediaPlayer.start();
        }

        setServiceState(ServiceState.PLAYING);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                this.onGainedAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                this.onLostAudioFocus(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                this.onLostAudioFocus(true);
                break;
            default:
        }
    }

    public void onGainedAudioFocus() {
        Log.d(LOG_TAG, "Gained audio focus.");
        mAudioFocus = AudioFocus.FOCUSED;

        if (ServiceState.PLAYING.equals(mState)) {
            configAndStartMediaPlayer();
        }
    }

    public void onLostAudioFocus(boolean canDuck) {
        Log.d(LOG_TAG, String.format("Lost audio focus. Can duck? %s", canDuck));
        mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            configAndStartMediaPlayer();
        }
    }

    public void setArtist(ParcelableArtist artist) {
        mArtist = artist;
    }

    public ArrayList<ParcelableTrack> getTracksList() {
        return mTracks;
    }

    public void setTracksList(ArrayList<ParcelableTrack> tracks) {
        mTracks = tracks;
    }

    public void selectTrack(int trackIndex) {
        mPosition = trackIndex;
    }

    public void startPlay() {
        mMediaPlayer.reset();

        ParcelableTrack currentTrack = getCurrentTrack();

        try {
            mMediaPlayer.setDataSource(currentTrack.getPreviewUrl());
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        tryToGetAudioFocus();
        setServiceState(ServiceState.PREPARING);
        mMediaPlayer.prepareAsync();

        String text = String.format(getResources().getString(R.string.notification_loading), getCurrentTrack().getTrackName());
        updateNotification(text);

        showNotification(text);
        mWifiLock.acquire();
    }

    public void continuePlaying() {
        if (!ServiceState.PAUSED.equals(mState)) {
            return;
        }

        tryToGetAudioFocus();
        mWifiLock.acquire();
        mMediaPlayer.start();
        activateProgressReporter();
        setServiceState(ServiceState.PLAYING);

        String text = String.format(getResources().getString(R.string.notification_playing), getCurrentTrack().getTrackName());
        updateNotification(text);
    }

    private void pause() {
        if (ServiceState.PLAYING.equals(mState)) {
            setServiceState(ServiceState.PAUSED);
            deactivateProgressReporter();

            String text = String.format(getResources().getString(R.string.notification_paused), getCurrentTrack().getTrackName());
            updateNotification(text);

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            releaseWifiLock();
        }
    }

    private void releaseResources(boolean releaseMediaPlayer) {
        stopForeground(true);
        if (releaseMediaPlayer) {
            releaseMediaPlayer();
        }
        releaseWifiLock();
        deactivateProgressReporter();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void togglePlayback() {
        switch (mState) {
            case STOPPED:
                startPlay();
                break;
            case PAUSED:
                continuePlaying();
                break;
            case PLAYING:
                pause();
                break;
            default:
                Log.e(LOG_TAG, String.format("Invalid state %s while toggling playback", mState));
        }
    }

    private void setServiceState(ServiceState state) {
        if (mState == state) {
            return;
        }

        if (ServiceState.PLAYING.equals(state)) {
            activateProgressReporter();
        } else {
            deactivateProgressReporter();
        }

        mState = state;

        Intent intent = new Intent(BROADCAST_PLAYER_STATE_CHANGED);
        intent.putExtra(BROADCAST_DATA_CURRENT_STATE, mState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private ParcelableTrack getCurrentTrack() {
        return mTracks.get(mPosition);
    }

    private void createWifiLock() {
        String lockId = String.format("%sLock", this.getClass().getSimpleName());
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, lockId);
    }

    private void releaseWifiLock() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.FOCUSED && this.requestAudioFocus()) {
            mAudioFocus = AudioFocus.FOCUSED;
        }
    }

    private void giveUpAudioFocus() {
        if (AudioFocus.FOCUSED.equals(mAudioFocus) && this.giveupAudioFocus()) {
            mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
        }
    }

    public boolean requestAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean giveupAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this);
    }

    private void updateNotification(String text) {
        Notification notification = buildNotification(text);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void showNotification(String text) {
        Notification notification = buildNotification(text);
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification(String text) {
        PendingIntent previousIntent = PendingIntent.getService(this, 0,
                new Intent(MediaPlayerService.ACTION_PREVIOUS),
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent togglePlaybackIntent = PendingIntent.getService(this, 0,
                new Intent(MediaPlayerService.ACTION_TOGGLE_PLAYBACK),
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent fowardIntent = PendingIntent.getService(this, 0,
                new Intent(MediaPlayerService.ACTION_FOWARD),
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        int togglePlaybackIcon = android.R.drawable.ic_media_play;
        if (ServiceState.PLAYING.equals(mState)) {
            togglePlaybackIcon = android.R.drawable.ic_media_pause;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
                .setContentIntent(pi)
                .setTicker(text)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setContentTitle(mArtist.getName())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, null, previousIntent)
                .addAction(togglePlaybackIcon, null, togglePlaybackIntent)
                .addAction(android.R.drawable.ic_media_next, null, fowardIntent);

        return builder.build();
    }

    private enum AudioFocus {
        NO_FOCUS_NO_DUCK,
        NO_FOCUS_CAN_DUCK,
        FOCUSED
    }

    public enum ServiceState {
        STOPPED,
        PREPARING,
        PLAYING,
        PAUSED
    }

    public class MediaPlayerServiceBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}