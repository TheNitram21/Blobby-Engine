package de.arnomann.martin.blobby.core;

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

public class Sound {

    private static boolean initialized = false;

    private static long deviceId;
    private static long context;

    private static ALCapabilities alCapabilities;
    private static ALCCapabilities alcCapabilities;

    private static List<Integer> sounds;

    private Sound() {}

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

    public static void playSound(String name) {
        name = BlobbyEngine.SOUNDS_PATH + name;

        // LOAD
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

        // PLAY
        int sourcePointer = alGenSources();
        sounds.add(sourcePointer);
        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
        alSourcePlay(sourcePointer);
    }

    public static void stopAllSounds() {
        for(int sound : sounds) {
            alSourceStop(sound);
            alDeleteSources(sound);
        }
    }

    public static void destroy() {
        if(initialized) {
            for(int sound : sounds) {
                alDeleteSources(sound);
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
