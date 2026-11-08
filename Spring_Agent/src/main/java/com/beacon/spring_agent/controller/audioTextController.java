package com.beacon.spring_agent.controller;


import com.openai.models.audio.transcriptions.Transcription;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audio")
public class audioTextController {
    private final TranscriptionModel transcriptionModel;

    public audioTextController(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }
    @GetMapping("/convert")
    public String transcribe(@Value("classpath:/Prompt/MySpring.mp3")Resource audio){
        AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(audio);
        AudioTranscriptionResponse audioTranscriptionResponse = transcriptionModel.call(audioTranscriptionPrompt);
        return audioTranscriptionResponse.getResult().getOutput();
    }
}
