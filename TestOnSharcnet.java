package PhD3;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class TestOnSharcnet {
    public static void main(String[] args) {

        List<Pair<String, Integer>> words = new ArrayList<>();
        words.add(new Pair<>("a", 2));
        words.add(new Pair<>("o", 4));
        words.add(new Pair<>("D", 1));
        words.add(new Pair<>("v", 3));
        words.add(new Pair<>("d", 5));

        System.out.println(words);
        words.sort(Comparator.comparing(Pair::getValue));
        System.out.println(words);

        List<Pair<Double, Double>> numbers = new ArrayList<>();
        numbers.add(new Pair<>(0.25, 2.0));
        numbers.add(new Pair<>(-5.2, 3.0));
        numbers.add(new Pair<>(8.99, 1.0));

        System.out.println(numbers);
        numbers.sort(Comparator.comparing(Pair::getValue));
        System.out.println(numbers);

        //int[] myRoute = {82, 83, 84, 85, 21, 22}; // based on matrix ALPTY
        //CVaR_Alg.cvarAlg(0.9999999, 31, myRoute, 5);

    }
}