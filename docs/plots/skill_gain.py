"""Regenerate the README chart: python docs/plots/skill_gain.py (requires matplotlib)."""
from pathlib import Path
import json

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def clamp(value, minimum, maximum):
    return max(minimum, min(maximum, value))


# GamePlaySettings.Skills defaults; one attempt per second, difficulty/cap 100.
BASE_GAIN = 0.00113654
BEGINNER_MULTIPLIER = 19.77
THRESHOLD = 30.0
CAP = 100.0
DIFFICULTY = 100.0


def progression():
    value, seconds = 0.0, 0.0
    hours, values = [0.0], [0.0]
    milestones = {}
    target = 10
    while value < CAP:
        chance = clamp((DIFFICULTY - value + 50) / 100, 0, 0.5)
        gain = (BASE_GAIN * clamp(1 - value / CAP, 0.1, 1)
                * clamp(DIFFICULTY / CAP, 0.5, 1.5)
                * (1 + (BEGINNER_MULTIPLIER - 1) * clamp(1 - value / THRESHOLD, 0, 1)))
        # Each success takes a geometrically distributed number of attempts.
        seconds += 1 / chance
        value = min(CAP, value + gain)
        hours.append(seconds / 3600)
        values.append(value)
        if value >= target:
            milestones[target] = seconds / 3600
            target += 10
    return hours, values, milestones


if __name__ == "__main__":
    hours, values, milestones = progression()
    plt.rcParams.update({"font.size": 11})
    figure, axes = plt.subplots(1, 2, figsize=(12, 4.8), gridspec_kw={"width_ratios": [1, 1.6]})
    for axis in axes:
        axis.plot(hours, values, color="#2369a1", linewidth=2)
        axis.grid(alpha=0.25)
        axis.set_xlabel("Tempo médio acumulado (horas)")
        axis.set_ylabel("Skill (pontos)")
    axes[0].set(xlim=(0, 3.1), ylim=(0, 31), title="Bônus inicial: 0 a 30")
    axes[1].set(xlim=(0, 150), ylim=(0, 102), title="Progressão completa: 0 a 100")
    axes[1].axhline(30, linestyle="--", color="#777777", linewidth=1)
    axes[1].scatter([milestones[30], milestones[100]], [30, 100], color="#2369a1", zorder=3)
    figure.suptitle("Ganho de skill — 1 tentativa/s, dificuldade 100 e cap 100")
    figure.text(0.5, 0.01, "Tempo esperado para atingir cada valor; trajetórias reais variam com o sorteio.", ha="center", fontsize=10)
    figure.tight_layout(rect=(0, 0.04, 1, 0.94))
    output = Path(__file__).resolve().parent
    figure.savefig(output / "skill-gain.png", dpi=170)
    figure.savefig(output / "skill-gain.svg")
    print(json.dumps(milestones, indent=2))
