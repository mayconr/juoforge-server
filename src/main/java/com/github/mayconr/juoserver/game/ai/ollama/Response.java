package com.github.mayconr.juoserver.game.ai.ollama;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private String chat;
    private String action;
}
