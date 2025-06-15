<template>
  <div class="game-container">
    <!-- 用户信息区 -->
    <div class="user-section">
      <input v-model="username" placeholder="输入昵称" class="nickname-input" />
      <button @click="startNewGame" class="start-btn">开始新游戏</button>
    </div>

    <!-- 游戏区 -->
    <div v-if="gameActive" class="game-section">
      <div class="element-selector">
        <div v-for="(_, index) in currentGuess" :key="index" class="element-item">
          <select v-model="currentGuess[index]" class="element-select">
            <option v-for="element in elements" :key="element" :value="element">
              {{ element }}
            </option>
          </select>
        </div>
        <button @click="submitGuess" class="submit-btn">提交猜测</button>
      </div>

      <!-- 结果显示 -->
      <div class="result-section">
        <div v-for="(guess, idx) in guessHistory" :key="idx" class="guess-row">
          <div class="guess-sequence">
            <span v-for="(element, i) in guess.sequence" :key="i" class="element-badge">
              {{ element }}
            </span>
          </div>
          <div class="guess-feedback">
            ✓ {{ guess.correctPos }} 个位置正确<br>
            ⚬ {{ guess.correctElem }} 个元素正确
          </div>
        </div>
      </div>
    </div>

    <!-- 游戏结束提示 -->
    <div v-if="gameCompleted" class="game-complete">
      <h3>恭喜 {{ username }}！</h3>
      <p>你在 {{ guessHistory.length }} 次尝试中破解了密码！</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      username: "",
      elements: ["金", "木", "水", "火", "土"],
      currentGuess: ["金", "金", "金", "金", "金"], // 默认选择
      guessHistory: [],
      gameActive: false,
      gameCompleted: false,
      gameId: null // 用于关联后端游戏ID
    };
  },
  methods: {
    async startNewGame() {
      if (!this.username.trim()) {
        alert("请输入昵称");
        return;
      }

      try {
        // 调用后端API创建新游戏
        const response = await axios.post('/api/games', {
          username: this.username
        });

        this.gameId = response.data.gameId;
        this.resetGame();
        this.gameActive = true;
        this.gameCompleted = false;

      } catch (error) {
        console.error("创建游戏失败:", error);
        alert("游戏创建失败，请重试");
      }
    },

    async submitGuess() {
      try {
        // 调用后端API验证猜测
        const response = await axios.post(`/api/games/${this.gameId}/guesses`, {
          guess: [...this.currentGuess]
        });

        const { correctPos, correctElem } = response.data;

        this.guessHistory.unshift({
          sequence: [...this.currentGuess],
          correctPos,
          correctElem
        });

        // 检查游戏是否完成
        if (correctPos === 5) {
          this.gameCompleted = true;
          this.gameActive = false;
        }

      } catch (error) {
        console.error("提交猜测失败:", error);
        alert("提交失败，请重试");
      }
    },

    resetGame() {
      this.currentGuess = ["金", "金", "金", "金", "金"];
      this.guessHistory = [];
    }
  }
};
</script>

<style scoped>

.game-container { max-width: 600px; margin: 2rem auto; padding: 20px; font-family: Arial, sans-serif; }
.user-section { display: flex; gap: 10px; margin-bottom: 20px; }
.nickname-input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; flex: 1; }
.start-btn, .submit-btn { padding: 8px 16px; background-color: #42b983; color: white; border: none; border-radius: 4px; cursor: pointer; }
.element-selector { display: flex; gap: 10px; margin-bottom: 20px; align-items: center; }
.element-item { flex: 1; }
.element-select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.result-section { border-top: 1px solid #eee; padding-top: 15px; }
.guess-row { display: flex; justify-content: space-between; padding: 10px; border-bottom: 1px solid #f0f0f0; }
.guess-sequence { display: flex; gap: 5px; }
.element-badge { display: inline-block; width: 30px; height: 30px; line-height: 30px; text-align: center; }
</style>