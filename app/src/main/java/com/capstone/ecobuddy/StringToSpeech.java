package com.capstone.ecobuddy;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by ariffia@mcmaster.ca on 2015-04-21.
 */
public class StringToSpeech
{
    public static TextToSpeech tts;

    /**
     * Call this in the activity on create
     * - Pass the activity's "this" to this method
     * @param mainActivity
     */
    public static void installTTS(MainActivity mainActivity) {
        tts = new TextToSpeech(
                mainActivity.getApplication(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            tts.setLanguage(Locale.US);
                        }
                    }
                }
        );
    }

    /**
     * Given that tts is installed, you can use this any where in the code
     * and it will say the passed message for you
     * - I need to use the depreciated method since the new version requires
     *   API 21 and above. This one works for API as low as 15
     * @param message
     */
    public static void sayThis(String message) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }
}

