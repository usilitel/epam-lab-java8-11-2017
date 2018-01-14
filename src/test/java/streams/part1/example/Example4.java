package streams.part1.example;

import lambda.data.JobHistoryEntry;
import lambda.data.Person;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("Java8CollectionRemoveIf")
public class Example4 {

    @Test
    public void putValueIfAbsentOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();

        if (!personSalaries.containsKey(alex)) {
            personSalaries.put(alex, 65_000);
        }

        assertEquals(65_000, personSalaries.get(alex).intValue());
    }

    @Test
    public void putValueIfAbsentUsingPutIfAbsent() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();

        personSalaries.putIfAbsent(alex, 65_000);

        assertEquals(65_000, personSalaries.get(alex).intValue());
    }

    private Integer hugeOperation() {
        try {
            TimeUnit.SECONDS.sleep(1);
            return 65_000;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void putHugeValueOnlyIfAbsentOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();

        if (!personSalaries.containsKey(alex)) {
            personSalaries.put(alex, hugeOperation());
        }

        assertEquals(65_000, personSalaries.get(alex).intValue());
    }

    @Test
    public void putHugeValueOnlyIfAbsentUsingComputeIfAbsent() throws InterruptedException {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();

        personSalaries.computeIfAbsent(alex, person -> hugeOperation());

        assertEquals(65_000, personSalaries.get(alex).intValue());
    }

    private static int raiseSalary(Person person, int salary) {
        return salary + 10_000 + person.getAge() * 100;
    }

    @Test
    public void reassignIfExistingKeyInMapUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        if (personSalaries.containsKey(alex)) {
            personSalaries.put(alex, raiseSalary(alex, personSalaries.get(alex)));
        }

        Integer ivanSalary = personSalaries.get(ivan);
        if (ivanSalary != null) {
            personSalaries.put(ivan, raiseSalary(alex, ivanSalary));
        }

        assertEquals(77_000, personSalaries.get(alex).intValue());
        assertNull(personSalaries.get(ivan));
    }

    @Test
    public void reassignOrDeleteSpecificKeyInMapUsingReplace() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        personSalaries.replace(alex, 65_000, raiseSalary(alex, 65_000));

        assertEquals(77_000, personSalaries.get(alex).intValue());
    }

    @Test
    public void reassignOrDeleteIfExistingKeyInMapUsingComputeIfPresent() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        personSalaries.computeIfPresent(alex, Example4::raiseSalary);
        personSalaries.computeIfPresent(ivan, Example4::raiseSalary);

        assertEquals(77_000, personSalaries.get(alex).intValue());
        assertNull(personSalaries.get(ivan));
    }

    @Test
    public void reassignExistingKeyInMapUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        personSalaries.put(alex, raiseSalary(alex, personSalaries.get(alex)));

        assertEquals(77_000, personSalaries.get(alex).intValue());
    }

    @Test
    public void reassignExistingKeyInMapUsingReplace() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        personSalaries.replace(alex, raiseSalary(alex, personSalaries.get(alex)));

        assertEquals(77_000, personSalaries.get(alex).intValue());
    }

    @Test
    public void reassignOrDeleteValuesInMapUsingCompute() {
        int baseSalary = 65_000;
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Person nick = new Person("Николай", "Очагов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, baseSalary);

        BiFunction<Person, Integer, Integer> getBasicOrRiseSalary = (person, salary) -> salary == null ? baseSalary
                                                                                                       : raiseSalary(person, salary);
        personSalaries.compute(alex, getBasicOrRiseSalary);
        personSalaries.compute(ivan, getBasicOrRiseSalary);
        personSalaries.compute(nick, getBasicOrRiseSalary);
        assertEquals(77_000, personSalaries.get(alex).intValue());
        assertEquals(65_000, personSalaries.get(ivan).intValue());
        assertEquals(65_000, personSalaries.get(nick).intValue());

        personSalaries.compute(nick, (person, salary) -> null);
        assertNull(personSalaries.get(nick));
    }

    @Test
    public void mergeValuesUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, List<JobHistoryEntry>> personsExperience = new HashMap<>();
        personsExperience.put(alex, new ArrayList<>(Collections.singleton(new JobHistoryEntry(1, "tester", "EPAM"))));

        List<JobHistoryEntry> newEpamExperience = new ArrayList<>(Arrays.asList(
                new JobHistoryEntry(1, "QA", "EPAM"),
                new JobHistoryEntry(1, "dev", "EPAM")
        ));
        if (personsExperience.containsKey(alex)) {
            personsExperience.get(alex).addAll(newEpamExperience);
        } else {
            personsExperience.put(alex, newEpamExperience);
        }
        assertEquals(3, personsExperience.get(alex).size());

        List<JobHistoryEntry> newGoogleExperience = new ArrayList<>(Arrays.asList(
                new JobHistoryEntry(1, "QA", "google"),
                new JobHistoryEntry(1, "dev", "google")
        ));
        List<JobHistoryEntry> alexExperience;
        if ((alexExperience = personsExperience.get(alex)) != null) {
            alexExperience.addAll(newGoogleExperience);
        } else {
            personsExperience.put(alex, newGoogleExperience);
        }
        assertEquals(5, personsExperience.get(alex).size());
    }

    @Test
    public void mergeValuesUsingMerge() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, List<JobHistoryEntry>> personsExperience = new HashMap<>();
        personsExperience.put(alex, new ArrayList<>(Collections.singleton(new JobHistoryEntry(1, "tester", "EPAM"))));

        List<JobHistoryEntry> newEpamExperience = new ArrayList<>(Arrays.asList(
                new JobHistoryEntry(1, "QA", "EPAM"),
                new JobHistoryEntry(1, "dev", "EPAM")
        ));
        personsExperience.merge(alex, newEpamExperience, Example4::mergeListsUsingStreams);
        assertEquals(3, personsExperience.get(alex).size());

        List<JobHistoryEntry> newGoogleExperience = new ArrayList<>(Arrays.asList(
                new JobHistoryEntry(1, "QA", "google"),
                new JobHistoryEntry(1, "dev", "google")
        ));
        personsExperience.merge(alex, newGoogleExperience, Example4::mergeListsUsingAddAll);
        assertEquals(5, personsExperience.get(alex).size());
    }

    private static <T> List<T> mergeListsUsingStreams(List<T> left, List<T> right) {
        return Stream.concat(left.stream(), right.stream()).collect(Collectors.toList());
    }

    private static <T> List<T> mergeListsUsingAddAll(List<T> left, List<T> right) {
        left.addAll(right);
        return left;
    }

    @Test
    public void updateAllValuesUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);
        personSalaries.put(ivan, 65_000);

        for (Person person : personSalaries.keySet()) {
            personSalaries.put(person, raiseSalary(person, personSalaries.get(person)));
        }

        Map<Person, Integer> expected = new HashMap<>();
        expected.put(alex, 77_000);
        expected.put(ivan, 77_400);
        assertEquals(expected, personSalaries);
    }

    @Test
    public void updateAllValuesUsingReplaceAll() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);
        personSalaries.put(ivan, 65_000);

        personSalaries.replaceAll(Example4::raiseSalary);

        Map<Person, Integer> expected = new HashMap<>();
        expected.put(alex, 77_000);
        expected.put(ivan, 77_400);
        assertEquals(expected, personSalaries);
    }

    @Test
    public void forEachEntryUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Person ivan = new Person("Иван", "Стрельцов", 24);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);
        personSalaries.put(ivan, 65_000);

        AtomicInteger sum = new AtomicInteger();
        personSalaries.forEach((person, salary) -> sum.addAndGet(raiseSalary(person, salary)));

        assertEquals(154_400, sum.get());
    }

    @Test
    public void removeIfUsingOldStyle() {
        Set<Person> peoples = new HashSet<>(Arrays.asList(
                new Person("Алексей", "Мельников", 20),
                new Person("Елена", "Рощина", 22),
                new Person("Иван", "Стрельцов", 24)
        ));

        for (Iterator<Person> iterator = peoples.iterator(); iterator.hasNext(); ) {
            Person people = iterator.next();
            if (people.getAge() > 23) {
                iterator.remove();
            }
        }

        assertEquals(2, peoples.size());
    }

    @Test
    public void removeIfUsingMethodFromJava8() {
        Set<Person> peoples = new HashSet<>(Arrays.asList(
                new Person("Алексей", "Мельников", 20),
                new Person("Елена", "Рощина", 22),
                new Person("Иван", "Стрельцов", 24)
        ));

        peoples.removeIf(person -> person.getAge() > 23);

        assertEquals(2, peoples.size());
    }

    @Test
    public void getOrDefaultUsingOldStyle() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        Integer salaryAlex = personSalaries.get(alex);
        if (salaryAlex == null) {
            salaryAlex = 30_000;
        }

        Integer salaryIvan = personSalaries.get(new Person("Иван", "Стрельцов", 24));
        if (salaryIvan == null) {
            salaryIvan = 30_000;
        }

        assertEquals(65_000, salaryAlex.intValue());
        assertEquals(30_000, salaryIvan.intValue());
    }

    @Test
    public void getOrDefaultUsingJava8() {
        Person alex = new Person("Алексей", "Мельников", 20);
        Map<Person, Integer> personSalaries = new HashMap<>();
        personSalaries.put(alex, 65_000);

        Person ivan = new Person("Иван", "Стрельцов", 24);

        assertEquals(65_000, personSalaries.getOrDefault(alex, 30_000).intValue());
        assertEquals(30_000, personSalaries.getOrDefault(ivan, 30_000).intValue());
    }
}
