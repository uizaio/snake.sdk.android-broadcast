
## Welcome to UIZA BroadCast Snake SDK

Simple Streaming at scale.

Uiza is the complete toolkit for building a powerful video streaming application with unlimited scalability. We design Uiza so simple that you only need a few lines of codes to start streaming, but sophisticated enough for you to build complex products on top of it.

Read [CHANGELOG here](https://github.com/uizaio/snake.sdk.android-broadcast/blob/master/CHANGELOG.md).

<br />
<br />

## Importing the Library
**Step 1. Add the `JitPack` repository to your `build.gradle` file**

```xml
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2. Add the dependency**

```xml
dependencies {
    implementation 'com.github.uizaio:snake.sdk.android-broadcast:x.y.z'
}
```

Get latest release number [HERE](httpshttps://github.com/uizaio/snake.sdk.android-broadcast/releases).

<br />
<br />
<br />
<br />

## How to Broadcast (Foreground)?:
It's very easy, plz follow these steps below to implement:

XML:

```xml
<com.uiza.broadcast.UZBroadCastView
    android:id="@+id/uzBroadCastView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />   
```

Pls take a look at class [`BroadCastAdvancedActivity`](https://github.com/uizaio/snake.sdk.android-broadcast/blob/master/samplebroadcast/src/main/java/com/uiza/rtpstreamer/broadcastAdvanced/BroadCastAdvancedActivity.kt) for more information.

Here are some points you need to remember:

```
uzBroadCastView.onSurfaceChanged =
            { _: SurfaceHolder, _: Int, _: Int, _: Int ->
                    uzBroadCastView.startPreview(
                    cameraFacing = CameraHelper.Facing.BACK,
                    width = 640,
                    height = 480,
                    rotation = CameraHelper.getCameraOrientation(this)
                    )
            }
```

```
    val linkStream = "Your link"
    uzBroadCastView.startStream(linkStream)
```
<br />

Enable or disable Anti aliasing (This method use FXAA).\
_AAEnabled – true is AA enabled, false is AA disabled. False by default.
```
    fun enableAA(AAEnabled: Boolean)
```
<br />

Set a filter to stream.\
You can select any filter or create your own filter if you extends from BaseFilterRender\
baseFilterRender – filter to set.\
You can modify parameters to filter after set it to stream

```
    fun setFilter(baseFilterRender: BaseFilterRender)
```
<br />

Capture an Image\
_takePhotoCallback – callback where you will get your image like a bitmap.
```
    fun takePhoto(takePhotoCallback: TakePhotoCallback)
```
<br />

Start camera preview. Ignored, if stream or preview is started.\
_cameraFacing – front or back camera. Like: CameraHelper.Facing.BACK CameraHelper.Facing.FRONT\
_width – of preview in px.\
_height – of preview in px.\
_rotation – camera rotation (0, 90, 180, 270). Recommended: CameraHelper.getCameraOrientation(Context)\
```
    fun startPreview(
        cameraFacing: CameraHelper.Facing,
        width: Int,
        height: Int,
        rotation: Int,
    )
```
<br />

Stop camera preview
```
    fun stopPreview()
```

<br />

Call this method before use @startStream. If not you will do a stream without audio.\
_bitrate – AAC in kb.\
_sampleRate – of audio in hz. Can be 8000, 16000, 22500, 32000, 44100.\
_isStereo – true if you want Stereo audio (2 audio channels), false if you want Mono audio (1 audio channel).\
_echoCanceler – true enable echo canceler, false disable.\
_noiseSuppressor – true enable noise suppressor, false disable.\
Returns: true if success, false if you get a error (Normally because the encoder selected doesn't support any configuration seated or your device hasn't a AAC encoder).

```
    fun prepareAudio(
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audioNoiseSuppressor: Boolean,
    )
```
<br />

Call this method before use @startStream. If not you will do a stream without video. NOTE: Rotation with encoder is silence ignored in some devices
Params
_width – resolution in px.\
_height – resolution in px.\
_fps – frames per second of the stream.\
_bitrate – H264 in bps.\
_rotation – could be 90, 180, 270 or 0. You should use CameraHelper.getCameraOrientation with SurfaceView or TextureView and 0 with OpenGlView or LightOpenGlView. NOTE: Rotation with encoder is silence ignored in some devices.\
Returns: true if success, false if you get a error (Normally because the encoder selected doesn't support any configuration seated or your device hasn't a H264 encoder).

```
    fun prepareVideo(
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        videoRotation: Int? = null,
    )
```

<br />

Start stream by url
```
    fun startStream(url: String) 
```

<br />

Stop stream started with @startStream.
```
    fun stopStream() 
```

<br />

Get list resolution of back camera
```
    fun getResolutionsBack(): List<CameraSize>
```

<br />

Get list resolution of front camera
```
    fun getResolutionsFront(): List<CameraSize>
```

<br />

Mute microphone, can be called before, while and after stream.
```
    fun disableAudio()
```

<br />

Enable a muted microphone, can be called before, while and after stream.
```
    fun enableAudio()
```

<br />

Get mute state of microphone.\
Returns: true if muted, false if enabled
```
    fun isAudioMuted(): Boolean?
```

<br />

Switch front and back camera
```
    fun switchCamera()
```

<br />

Starts recording an MP4 video. Needs to be called while streaming\
Params:\
_path – Where file will be saved.\
Throws: IOException – If initialized before a stream.
```
    fun startRecord(path: String)
```

<br />

Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
```
    fun stopRecord()
```

<br />

Change preview orientation can be called while stream.\
Params:\
_orientation – of the camera preview. Could be 90, 180, 270 or 0.
```
    fun setPreviewOrientation(orientation: Int)
```
<br />
<br />
<br />
<br />

## How to Broadcast (Background)?:
```
    <com.uiza.background.UZBackgroundView
        android:id="@+id/uzBackgroundView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

Pls take a look at class [`BackgroundAdvancedActivity`](https://github.com/uizaio/snake.sdk.android-broadcast/blob/master/samplebroadcast/src/main/java/com/uiza/rtpstreamer/backgroundAdvanced/BackgroundAdvancedActivity.kt) for more information.

Here are some points you need to remember:

<br />

Start camera preview. Ignored, if stream or preview is started.
```
    fun startPreview(
        videoWidth: Int,
        videoHeight: Int,
    )
```

<br />

Stop camera preview. Ignored if streaming or already stopped. You need call it after
```
    fun stopPreview() 
```

<br />

Strop straming
```
    fun stopStream()
```

<br />

Start straming
```
    fun startStream(
        endPoint: String,
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audiNoiseSuppressor: Boolean
    )
```

<br />

Get supported preview resolutions of back camera in px
Return list of preview resolutions supported by back camera
```
    fun getResolutionsBack(): List<CameraSize>
```

<br />

Get supported preview resolutions of front camera in px.\
Return list of preview resolutions supported by front camera
```
    fun getResolutionsFront(): List<CameraSize> 
```

<br />

Mute microphone, can be called before, while and after stream.
```
    fun disableAudio()
```

<br />

Enable a muted microphone, can be called before, while and after stream.
```
    fun enableAudio()
```

<br />

Get mute state of microphone.
```
    fun isAudioMuted()
```

<br />

Switch camera used. Can be called anytime\
throws CameraOpenException If the other camera doesn't support same resolution.
```
    fun switchCamera()
```

<br />

Get stream state.\
Reeturn true if streaming, false if not streaming.
```
    fun isStreaming(): Boolean?
```

<br />
<br />
<br />
<br />


## How to broadcast your screen?

```
        <com.uiza.display.UZDisplayView
        android:id="@+id/uzDisplayBroadCast"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

See example in class [`DisplayAdvancedActivity`](https://github.com/uizaio/snake.sdk.android-broadcast/blob/master/samplebroadcast/src/main/java/com/uiza/rtpstreamer/displayAdvanced/DisplayAdvancedActivity.kt):

Here are some points you need to remember:

<br />

Get stream state.\
Returns:\
_true if streaming, false if not streaming.
```
    fun isStreaming(): Boolean
```

<br />

Get record state.\
Returns:\
_true if recording, false if not recoding.
```
    fun isRecording(): Boolean
```

<br />

Init stream config by this function
```
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        endPoint: String,
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        videoRotation: Int,
        videoDpi: Int,
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audioNoiseSuppressor: Boolean,
    )
```

<br />

Starts recording an MP4 video. Needs to be called while streaming.\
Params:\
_path – Where file will be saved.\
Throws: IOException – If initialized before a stream.
```
    fun startRecord(path: String, listener: RecordController.Listener?)
```

<br />

Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
```
    fun stopRecord() 
```

<br />

Mute microphone, can be called before, while and after stream.
```
    fun disableAudio()
```

<br />

Enable a muted microphone, can be called before, while and after stream.
```
    fun enableAudio()
```

<br />

Get mute state of microphone.\
Returns: true if muted, false if enabled
```
    fun isAudioMuted(): Boolean?
```


<br />
<br />
<br />
<br />


## Features:

- [x] Android min API 21.
- [x] Support [camera1](https://developer.android.com/reference/android/hardware/Camera.html) and [camera2](https://developer.android.com/reference/android/hardware/camera2/package-summary.html) API
- [x] Encoder type buffer to buffer.
- [x] Encoder type surface to buffer.
- [x] RTMP/RTMPS auth.
- [x] Audio noise suppressor.
- [x] Audio echo cancellation.
- [x] Disable/Enable video and audio while streaming.
- [x] Switch camera while streaming.
- [x] Change video bitrate while streaming.
- [X] Get upload bandwidth used.
- [X] Record MP4 file while streaming.
- [x] H264 and AAC hardware encoding.
- [x] Force H264 and AAC Codec hardware/software encoding (Not recommended).
- [x] Stream device display.
- [X] OpenGL real time filters and watermarks. [More info](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java/wiki/Real-time-filters)

<br />
<br />
<br />
<br />

## For contributors

Uiza Checkstyle configuration is based on the Google coding conventions from Google Java Style
that can be found at [here](https://google.github.io/styleguide/javaguide.html).
<br />
<br />
<br />
<br />

## Supported devices

Support all devices which have ***Android 5.0 (API level 21) above.***
For a given use case, we aim to support UizaSDK on all Android devices that satisfy the minimum version requirement.

**Note:** Some Android emulators do not properly implement components of Android’s media stack, and as a result do not support UizaSDK. This is an issue with the emulator, not with UizaSDK. Android’s official emulator (“Virtual Devices” in Android Studio) supports UizaSDK provided the system image has an API level of at least 23. System images with earlier API levels do not support UizaSDK. The level of support provided by third party emulators varies. Issues running UizaSDK on third party emulators should be reported to the developer of the emulator rather than to the UizaSDK team. Where possible, we recommend testing media applications on physical devices rather than emulators.

<br />
<br />
<br />
<br />

## Support

If you've found an error in this sample, please file an [issue ](https://github.com/uizaio/snake.sdk.android-broadcast/issues)

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub. Please feel free to contact me anytime: loitp@uiza.io for more details.

Email: _loitp@uiza.io_
Website: _[uiza.io](https://uiza.io/)_
<br />
<br />
<br />
<br />

## License

UizaSDK is released under the BSD license. See  [LICENSE](https://github.com/uizaio/snake.sdk.android-broadcast/blob/master/LICENSE)  for details.


