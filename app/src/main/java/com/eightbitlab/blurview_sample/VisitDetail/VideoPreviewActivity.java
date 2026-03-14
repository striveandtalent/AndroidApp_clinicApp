package com.eightbitlab.blurview_sample.VisitDetail;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.net.VideoPlayerCache;

import okhttp3.OkHttpClient;

public class VideoPreviewActivity extends AppCompatActivity {

    private PlayerView playerView;
    private TextView tvBack;
    private TextView tvTitle;
    private ExoPlayer player;

    private long playbackPosition = 0L;
    private boolean playWhenReady = true;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        playerView = findViewById(R.id.playerView);
        tvBack = findViewById(R.id.tvBack);
        tvTitle = findViewById(R.id.tvTitle);

        String url = getIntent().getStringExtra("url");
        String fileName = getIntent().getStringExtra("fileName");

        tvTitle.setText(fileName == null || fileName.trim().isEmpty() ? "视频预览" : fileName);
        tvBack.setOnClickListener(v -> finish());

        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "视频地址为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("VIDEO_PREVIEW", "url = " + url);

        initPlayer(url, fileName);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer(String url, String fileName) {
        OkHttpClient okHttpClient = ApiClient.getUnsafeOkHttpClient();

        OkHttpDataSource.Factory upstreamFactory =
                new OkHttpDataSource.Factory(okHttpClient);

        DataSource.Factory cacheFactory =
                new CacheDataSource.Factory()
                        .setCache(VideoPlayerCache.getInstance(this))
                        .setUpstreamDataSourceFactory(upstreamFactory)
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        String stableCacheKey = buildStableCacheKey(url, fileName);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .setCustomCacheKey(stableCacheKey)
                .build();

        ProgressiveMediaSource mediaSource =
                new ProgressiveMediaSource.Factory(cacheFactory)
                        .createMediaSource(mediaItem);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.setMediaSource(mediaSource);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(playbackPosition);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d("VIDEO_PREVIEW", "state=" + playbackState);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e("VIDEO_PREVIEW", "player error", error);
                Toast.makeText(VideoPreviewActivity.this,
                        "视频播放失败：" + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String buildStableCacheKey(String url, String fileName) {
        String safeName = fileName == null ? "" : fileName.trim();
        if (!safeName.isEmpty()) {
            return "video_" + safeName.toLowerCase();
        }

        int qIndex = url == null ? -1 : url.indexOf('?');
        String pureUrl = (url == null) ? "" : (qIndex >= 0 ? url.substring(0, qIndex) : url);
        return "video_" + pureUrl.toLowerCase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
}