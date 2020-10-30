package net.teamfruit.sushida.player.state;

import net.teamfruit.sushida.mode.GameMode;
import net.teamfruit.sushida.player.StateContainer;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResultState implements IState {
    private Iterator<Consumer<StateContainer>> showMessage;

    @Override
    public IState onEnter(StateContainer state) {
        Player player = state.data.player;

        // ランキング算出
        GameMode mode = state.data.getGroup().getMode();
        List<Integer> board = state.data.getGroup().getPlayers().stream()
                .map(e -> mode.getScore(e.getSession()))
                .sorted(mode.getScoreComparator())
                .collect(Collectors.toList());
        int myScore = mode.getScore(state);
        state.ranking = board.indexOf(myScore) + 1;

        // クリア
        player.sendTitle("", "", 0, 0, 0);
        IntStream.range(0, 20).forEachOrdered(e -> player.sendMessage(""));

        // ガッ
        player.playSound(player.getLocation(), "sushida:sushida.gan", SoundCategory.PLAYERS, 1, 1);

        showMessage = state.data.getGroup().getMode().getResultMessageTasks();

        return null;
    }

    @Override
    public IState onTick(StateContainer state) {
        if (showMessage.hasNext())
            showMessage.next().accept(state);
        else if (state.data.getGroup().hasPermission(state.data)) {
            // ゲーム終了
            state.data.destroy();
            return new NoneState();
        }
        return null;
    }
}
