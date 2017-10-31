package lambda.part1.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal", "CodeBlock2Expr"})
public class Example3 {

    /**
     * SAM-interface, предназначенный для суммирования элементов.
     * @param <T> Тип суммируемых элементов.
     */
    @FunctionalInterface
    private interface Summator<T> {

        T sum(T left, T right);

        default T twice(T value) {
            return sum(value, value);
        }
    }

    @Test
    public void anonymousClassImplementation() {
        Summator<Integer> sum = new Summator<Integer>() {
            @Override
            public Integer sum(Integer left, Integer right) {
                return left + right;
            }
        };

        assertEquals(3, sum.sum(1, 2).intValue());
        assertEquals(1, sum.sum(-1, 2).intValue());
        assertEquals(4, sum.twice(2).intValue());
        assertEquals(0, sum.twice(0).intValue());
    }

    @Test
    public void statementLambdaImplementation() {
        // FIXME вывод типов
        Summator<Integer> sum = (Integer left, Integer right) -> {
            return left + right;
        };

        assertEquals(3, sum.sum(1, 2).intValue());
        assertEquals(1, sum.sum(-1, 2).intValue());
        assertEquals(4, sum.twice(2).intValue());
        assertEquals(0, sum.twice(0).intValue());
    }

    @Test
    public void expressionLambdaImplementation() {
        Summator<Integer> sum = (left, right) -> left + right;

        assertEquals(3, sum.sum(1, 2).intValue());
        assertEquals(1, sum.sum(-1, 2).intValue());
        assertEquals(4, sum.twice(2).intValue());
        assertEquals(0, sum.twice(0).intValue());
    }

    private static String stringSum(String left, String right) {
        return left + right;
    }

    @Test
    public void classMethodReferenceLambdaImplementation() {
        Summator<String> sum = Example3::stringSum;

        assertEquals("ab", sum.sum("a", "b"));
        assertEquals("aa", sum.twice("a"));
    }

    private final String delimiter = "-";

    private String stringSumWithDelimiter(String left, String right) {
        return left + delimiter + right;
    }

    @Test
    public void objectMethodReferenceLambdaImplementation() {
        Summator<String> sum = this::stringSumWithDelimiter;

        assertEquals("a-b", sum.sum("a", "b"));
        assertEquals("a-a", sum.twice("a"));
    }
}
