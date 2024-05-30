package org.javaacademy.gaming_machine.service;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.javaacademy.gaming_machine.entity.FinanceResult;
import org.javaacademy.gaming_machine.entity.Game;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class GameService {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private static final int TEN = 10;
    private static final int TWENTY = 20;
    private static final int FIFTY = 50;
    private final String TRUNCATE_FINANCE_RESULT = "truncate machine.finance_result";
    private final String TRUNCATE_GAME = "truncate machine.game";
    private final String INSERT_ZERO = "insert into machine.finance_result values(0, 0)";


    @PostConstruct
    public void init() {
        jdbcTemplate.execute(TRUNCATE_FINANCE_RESULT);
        jdbcTemplate.execute(TRUNCATE_GAME);
        jdbcTemplate.execute(INSERT_ZERO);
    }

    public String play() {
        AtomicReference<String> action = new AtomicReference<>("");
        String thrownSymbols = makeCombination();
        writeCombination(thrownSymbols);
        transactionTemplate.executeWithoutResult((transactionStatus -> {
            Object savePoint = transactionStatus.createSavepoint();
            jdbcTemplate.execute(putCoin());
            switch (thrownSymbols) {
                case ("AAA"):
                    nextAction(action, thrownSymbols, TEN, printWin(TEN));
                    break;
                case ("FFF"):
                    nextAction(action, thrownSymbols, TWENTY, printWin(TWENTY));
                    break;
                case ("DDD"):
                    nextAction(action, thrownSymbols, FIFTY, printWin(FIFTY));
                    break;
                case ("AFD"):
                    transactionStatus.rollbackToSavepoint(savePoint);
                    action.set("бесплатный ход");
                    break;
                default:
                    nextAction(action, thrownSymbols, 0, "Вы ничего не выиграли");
                    break;
            }
        }));
        return action.toString();
    }

    public String showHistory() {
        String sqlForFinanceResult = "select * from machine.finance_result limit 1";
        String sqlForGame = "select * from machine.game order by id DESC limit 5";
        List<FinanceResult> financeResultList = jdbcTemplate.query(sqlForFinanceResult,
                this::financeResultRowMapper);

        List<Game> gameList = jdbcTemplate.query(sqlForGame, this::gameRowMapper);
        String result = """
                        {
                        "playerIncome" : %d,
                        "playerOutcome : %d,
                        "game_history : %s
                        }
                        """.formatted(financeResultList.get(0).getPlayerIncome(),
                financeResultList.get(0).getPlayerOutcome(),
                gameList.stream().map(game -> game.getFirstSymbol() + game.getSecondSymbol()
                        + game.getThirdSymbol()).collect(Collectors.toList()));
        return result;
    }

    private void nextAction(AtomicReference<String> action,
                            String thrownSymbols,
                            int money,
                            String message) {
        updateIncome(money);
        action.set(String.format("%s\n%s",
                thrownSymbols.replace("", " ").trim(), message));
    }

    private FinanceResult financeResultRowMapper(ResultSet resultSet, int rowNum)
            throws SQLException {
        FinanceResult financeResult = new FinanceResult();
        financeResult.setPlayerIncome(resultSet.getInt("income"));
        financeResult.setPlayerOutcome(resultSet.getInt("outcome"));
        return financeResult;
    }

    private Game gameRowMapper(ResultSet resultSet, int rowNum) throws SQLException {
        Game game = new Game();
        game.setId(resultSet.getInt("id"));
        game.setFirstSymbol(resultSet.getString("sym_1"));
        game.setSecondSymbol(resultSet.getString("sym_2"));
        game.setThirdSymbol(resultSet.getString("sym_3"));
        return game;

    }

    private String putCoin() {
        return "update machine.finance_result set outcome = outcome + 15";
    }

    private String makeCombination() {
        String symbols = "AFD";
        return new Random().ints(3, 0, symbols.length())
                .mapToObj(symbols::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    private String printWin(int income) {
        return "Вы выиграли %d".formatted(income);
    }

    private void updateIncome(int income) {
        int incomeNow = jdbcTemplate.update(
                "update machine.finance_result set income = income + " + income);
    }

    private void writeCombination(String combo) {
        String[] symbols = combo.split("");
        String sql = "insert into machine.game(sym_1, sym_2, sym_3) values(?, ?, ?)";
        jdbcTemplate.update(sql, symbols[0], symbols[1], symbols[2]);
    }

}
