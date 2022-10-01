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

/**
 * A class for playing sound. Sound has to be in the <i>OGG Vorbis</i> format.
 */
public class SoundPlayer {

    private static boolean initialized = false;

    private static long deviceId;
    private static long context;

    private static ALCapabilities alCapabilities;
    private static ALCCapabilities alcCapabilities;

    private static List<Sound> sounds;

    private SoundPlayer() {}

    /**
     * Initializes the sound player.
     */
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

    /**
     * Creates a new instance of the sound class.
     * @param name the path to the sound file.
     * @return the created sound file.
     */
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

    /**
     * Plays a sound from a path.
     * @param name the path.
     * @return the played sound.
     */
    public static Sound playSound(String name, boolean looping) {
        Sound sound = createSound(name);
        name = BlobbyEngine.SOUNDS_PATH + name;
        playSound(sound, looping);
        return sound;
    }

    /**
     * Plays a sound from a sound instance.
     * @param sound the sound to play.
     */
    public static void playSound(Sound sound, boolean looping) {
        sounds.add(sound);
        alSourcei(sound.getSourcePointer(), AL_BUFFER, sound.getBufferPointer());
        alSourcei(sound.getSourcePointer(), AL_LOOPING, looping ? 1 : 0);
        alSourcePlay(sound.getSourcePointer());
    }

    /**
     * Stops all sounds.
     */
    public static void stopAllSounds() {
        for(Sound sound : sounds) {
            stopAndDeleteSound(sound);
        }
    }

    /**
     * Stops a specific sound.
     * @param sound the sound to stop.
     */
    public static void stopSound(Sound sound) {
        alSourceStop(sound.getSourcePointer());
    }

    /**
     * Stops a specific sound. It can not be played again afterwards.
     * @param sound the sound to stop and delete.
     */
    public static void stopAndDeleteSound(Sound sound) {
        stopSound(sound);
        alDeleteSources(sound.getSourcePointer());
        sounds.remove(sound);
    }

    /**
     * Destroys the sound player. This will stop all sounds.
     */
    public static void destroy() {
        if(initialized) {
            for(Sound sound : sounds) {
                alDeleteSources(sound.getSourcePointer());
            }
            sounds.clear();

            alcDestroyContext(context);
            alcCloseDevice(deviceId);

            initialized = false;
        }
    }

    /**
     * Returns whether the sound player is initialized or not.
     * @return {@code true} if the sound player is initialized, {@code false} otherwise.
     */
    public static boolean isInitialized() {
        return initialized;
    }

}
