package de.arnomann.martin.blobby.sound;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class SoundPlayer {

    private static boolean initialized = false;

    private static long deviceId;
    private static long context;

    private static ALCapabilities alCapabilities;
    private static ALCCapabilities alcCapabilities;

    private static List<Sound> sounds;

    private SoundPlayer() {}

    public static void initialize() {
        if(!initialized) {
            sounds = new ArrayList<>();

            deviceId = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));

            int[] attributes = { 0 };
            context = alcCreateContext(deviceId, attributes);
            alcMakeContextCurrent(context);

            alcCapabilities = ALC.createCapabilities(deviceId);
            alCapabilities = AL.createCapabilities(alcCapabilities);

            initialized = true;
        }
    }

    public static Sound createSound(String name) {
        name = BlobbyEngine.SOUNDS_PATH + name;

        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(name, channelsBuffer, sampleRateBuffer);

        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();

        stackPop();
        stackPop();

        int format = -1;
        if (channels == 1)
            format = AL_FORMAT_MONO16;
        else if (channels == 2)
            format = AL_FORMAT_STEREO16;

        int bufferPointer = alGenBuffers();
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);
        free(rawAudioBuffer);

        int sourcePointer = alGenSources();

        return new Sound(name, sourcePointer, bufferPointer);
    }

    public static Sound playSound(String name) {
        Sound sound = createSound(name);
        name = BlobbyEngine.SOUNDS_PATH + name;
        sounds.add(sound);
        playSound(sound);
        return sound;
    }

    public static void playSound(Sound sound) {
        alSourcei(sound.getSourcePointer(), AL_BUFFER, sound.getBufferPointer());
        alSourcePlay(sound.getSourcePointer());
    }

    public static void stopAllSounds() {
        for(Sound sound : sounds) {
            stopAndDeleteSound(sound);
        }
    }

    public static void stopSound(Sound sound) {
        alSourceStop(sound.getSourcePointer());
    }

    public static void stopAndDeleteSound(Sound sound) {
        stopSound(sound);
        alDeleteSources(sound.getSourcePointer());
        sounds.remove(sound);
    }

    public static void destroy() {
        if(initialized) {
            for(Sound sound : sounds) {
                alDeleteSources(sound.getSourcePointer());
                sounds.remove(sound);
            }

            alcDestroyContext(context);
            alcCloseDevice(deviceId);

            initialized = false;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

}
