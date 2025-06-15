package org.L2.untitled.controller;

import org.L2.untitled.model.GameRecord;
import org.L2.untitled.repository.GameRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class GameController {

    // 内存中存储游戏会话
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    // 数据库仓库（只用于保存记录）
    private final GameRecordRepository recordRepository;

    @Autowired
    public GameController(GameRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    // 创建新游戏
    @PostMapping("/games")
    public Map<String, String> createGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String sessionId = UUID.randomUUID().toString();

        // 生成答案（金木水火土的随机组合）
        List<String> elements = Arrays.asList("金", "木", "水", "火", "土");
        Collections.shuffle(elements);
        List<String> answer = elements.subList(0, 5);

        // 存储在内存中
        activeSessions.put(sessionId, new GameSession(username, answer));

        return Map.of("gameId", sessionId);
    }

    // 提交猜测
    @PostMapping("/games/{gameId}/guesses")
    public Map<String, Integer> submitGuess(
            @PathVariable String gameId,
            @RequestBody Map<String, List<String>> request) {

        GameSession session = activeSessions.get(gameId);
        if (session == null) {
            throw new RuntimeException("游戏会话不存在或已过期");
        }

        List<String> guess = request.get("guess");
        int[] result = checkGuess(session.answer, guess);

        // 更新尝试次数
        session.attempts++;

        // 如果猜对，保存记录并移除会话
        if (result[0] == 5) {
            saveGameResult(session);
            activeSessions.remove(gameId);
        }

        return Map.of(
                "correctPos", result[0],
                "correctElem", result[1]
        );
    }

    // 验证猜测逻辑
    private int[] checkGuess(List<String> answer, List<String> guess) {
        int correctPos = 0; // 位置+元素正确
        int correctElem = 0; // 仅元素正确
        boolean[] matched = new boolean[5];

        // 检查位置正确的元素
        for (int i = 0; i < 5; i++) {
            if (guess.get(i).equals(answer.get(i))) {
                correctPos++;
                matched[i] = true;
            }
        }

        // 检查元素存在但位置不正确的
        for (int i = 0; i < 5; i++) {
            if (matched[i]) continue;
            for (int j = 0; j < 5; j++) {
                if (!matched[j] && guess.get(i).equals(answer.get(j))) {
                    correctElem++;
                    matched[j] = true;
                    break;
                }
            }
        }

        return new int[]{correctPos, correctElem};
    }

    // 保存游戏结果到数据库
    private void saveGameResult(GameSession session) {
        GameRecord record = new GameRecord();
        record.setUsername(session.username);
        record.setAttempts(session.attempts);
        recordRepository.save(record);
    }

    // 内部游戏会话类（不持久化）
    private static class GameSession {
        String username;
        List<String> answer;
        int attempts = 0;

        GameSession(String username, List<String> answer) {
            this.username = username;
            this.answer = answer;
        }
    }
}